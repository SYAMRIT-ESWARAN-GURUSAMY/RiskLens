package sentinelx.service;

import sentinelx.exception.BankingException;
import sentinelx.fraud.FraudEngine;
import sentinelx.fraud.RiskAssessment;
import sentinelx.model.*;
import sentinelx.security.AuthService;
import sentinelx.security.TotpService;
import java.time.LocalDateTime;
import java.util.*;

/** Coordinates transactions, fraud decisions, behavior learning, and audit records. */
public final class BankingService {
    private final Map<String, Account> accounts = new HashMap<>();
    private final FraudEngine fraudEngine = new FraudEngine();
    private final AuditLogger auditLogger = new AuditLogger();
    private final List<Transaction> transactions = new ArrayList<>();
    private final Map<String, Integer> failedPinAttempts = new HashMap<>();
    private final AuthService authService = new AuthService();
    private final SessionManager sessionManager = new SessionManager();
    private final AlertService alertService = new AlertService();
    private final AccountStore accountStore = new AccountStore();
    private final boolean persistenceEnabled;
    private EventMonitor eventMonitor;
    public BankingService() { this(true); }
    /** Allows console demonstrations to run without changing real local simulator data. */
    public BankingService(boolean persistenceEnabled) { this.persistenceEnabled = persistenceEnabled; }
    public void register(Account account) { accounts.put(account.accountNumber(), account); saveAccounts(); }
    public void loadSavedAccounts() { accountStore.load().forEach(account -> accounts.put(account.accountNumber(), account)); }
    private void saveAccounts() { if (persistenceEnabled) accountStore.save(accounts()); }
    public Optional<Account> findAccount(String number) { return Optional.ofNullable(accounts.get(number)); }
    public List<Transaction> transactions() { return List.copyOf(transactions); }
    public List<Account> accounts() { return accounts.values().stream().sorted(Comparator.comparing(Account::accountNumber)).toList(); }
    public List<SecurityAlert> alerts() { return alertService.all(); }
    public int activeSessions() { return sessionManager.activeCount(); }
    public AuditVerification verifyAuditLog() { return auditLogger.verify(); }
    public void setEventMonitor(EventMonitor monitor) { eventMonitor = monitor; }
    /** Authenticates an account and records failed-attempt state for lockout. */
    public void authenticate(String number, String pin) throws sentinelx.exception.AuthenticationException {
        Account account = findAccount(number).orElseThrow(() -> new sentinelx.exception.AuthenticationException("Account was not found"));
        int failures = failedPinAttempts.getOrDefault(number, 0);
        try {
            authService.authenticate(account, pin, failures);
            failedPinAttempts.remove(number);
            auditLogger.log("LOGIN", "Authenticated account " + number);
        } catch (sentinelx.exception.AuthenticationException exception) {
            failedPinAttempts.put(number, failures + 1);
            auditLogger.log("FAILED_LOGIN", "Account " + number + ": " + exception.getMessage());
            alertService.publish(new SecurityAlert(LocalDateTime.now(), account.frozen() ? AlertSeverity.CRITICAL : AlertSeverity.WARNING, number, "Authentication failed", exception.getMessage()));
            throw exception;
        }
    }
    /** Opens a short-lived authenticated session for a user interface or channel simulator. */
    public Session login(String number, String pin) throws sentinelx.exception.AuthenticationException {
        authenticate(number, pin);
        Session session = sessionManager.open(number);
        auditLogger.log("SESSION_OPEN", "Session opened for " + number);
        return session;
    }
    public void logout(String token) { sessionManager.close(token); auditLogger.log("SESSION_CLOSE", "Session closed"); }
    public void changePin(String number, String oldPin, String newPin) throws BankingException {
        try { authenticate(number, oldPin); }
        catch (sentinelx.exception.AuthenticationException exception) { throw new BankingException(exception.getMessage()); }
        Account account = findAccount(number).orElseThrow(() -> new BankingException("Account was not found"));
        authService.setPin(account, newPin); saveAccounts(); auditLogger.log("PIN_CHANGE", "PIN changed for " + number);
        alertService.publish(new SecurityAlert(LocalDateTime.now(), AlertSeverity.INFO, number, "PIN changed", "A PIN change was recorded."));
    }
    public void unfreeze(String accountNumber) throws BankingException {
        Account account = findAccount(accountNumber).orElseThrow(() -> new BankingException("Account was not found"));
        account.unfreeze(); saveAccounts(); auditLogger.log("ACCOUNT_UNFROZEN", "Administrator unfroze " + accountNumber);
        alertService.publish(new SecurityAlert(LocalDateTime.now(), AlertSeverity.INFO, accountNumber, "Account unfrozen", "Administrator action"));
    }
    public String currentTotpCode(String accountNumber) throws BankingException {
        Account account = findAccount(accountNumber).orElseThrow(() -> new BankingException("Account was not found"));
        return TotpService.currentCode(account.totpSecret());
    }
    public RiskAssessment transact(Session session, TransactionType type, Channel channel, double amount, String terminal, String totpCode) throws BankingException {
        if (session == null || sessionManager.valid(session.token()).filter(valid -> valid.accountNumber().equals(session.accountNumber())).isEmpty())
            throw new BankingException("Your session has expired. Authenticate again.");
        return transactInternal(session.accountNumber(), type, channel, amount, terminal, totpCode);
    }
    public RiskAssessment transact(String number, TransactionType type, Channel channel, double amount, String terminal) throws BankingException {
        return transactInternal(number, type, channel, amount, terminal, null);
    }
    private RiskAssessment transactInternal(String number, TransactionType type, Channel channel, double amount, String terminal, String totpCode) throws BankingException {
        Account account = findAccount(number).orElseThrow(() -> new BankingException("Account was not found"));
        if (account.frozen()) throw new BankingException("Account is frozen");
        Transaction transaction = new Transaction(UUID.randomUUID().toString(), number, type, channel, amount, terminal, LocalDateTime.now());
        RiskAssessment assessment = fraudEngine.assess(account, transaction);
        if (assessment.decision() == Decision.FREEZE) { account.freeze(); saveAccounts(); auditLogger.log("FROZEN", assessment.explanation()); alertService.publish(new SecurityAlert(LocalDateTime.now(), AlertSeverity.CRITICAL, number, "Account frozen", assessment.explanation())); throw new BankingException("Transaction rejected and account frozen: " + assessment.explanation()); }
        if (assessment.decision() == Decision.CHALLENGE && !TotpService.matches(account.totpSecret(), totpCode)) { auditLogger.log("CHALLENGE", assessment.explanation()); alertService.publish(new SecurityAlert(LocalDateTime.now(), AlertSeverity.WARNING, number, "Verification challenge", assessment.explanation())); throw new BankingException("TOTP verification required. Demo code: " + TotpService.currentCode(account.totpSecret())); }
        try {
            switch (type) { case WITHDRAWAL -> account.withdraw(amount); case DEPOSIT -> account.deposit(amount); case BALANCE_ENQUIRY -> { } }
        } catch (IllegalStateException exception) { throw new BankingException(exception.getMessage()); }
        transactions.add(transaction); account.behaviourProfile().learn(transaction); saveAccounts(); auditLogger.log(type.name(), assessment.explanation());
        if (assessment.decision() == Decision.MONITOR) alertService.publish(new SecurityAlert(LocalDateTime.now(), AlertSeverity.WARNING, number, "Transaction monitored", assessment.explanation()));
        if (eventMonitor != null) eventMonitor.publish(transaction);
        return assessment;
    }
}

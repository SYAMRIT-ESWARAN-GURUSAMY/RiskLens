package sentinelx.model;

import java.util.Objects;
import java.time.Instant;

/** Abstract base type demonstrating inheritance and controlled state changes. */
public abstract class Account {
    private final String accountNumber;
    private final String customerName;
    private String pinHash;
    private String pinSalt;
    private double balance;
    private boolean frozen;
    private String totpSecret;
    private Instant pinChangedAt = Instant.now();
    private final BehaviourProfile behaviourProfile = new BehaviourProfile();

    protected Account(String accountNumber, String customerName, double openingBalance) {
        this.accountNumber = Objects.requireNonNull(accountNumber);
        this.customerName = Objects.requireNonNull(customerName);
        if (openingBalance < 0) throw new IllegalArgumentException("Opening balance cannot be negative");
        balance = openingBalance;
    }
    public abstract String accountKind();
    public abstract boolean canWithdraw(double amount);
    public void withdraw(double amount) { if (!canWithdraw(amount)) throw new IllegalStateException("Insufficient permitted balance"); balance -= amount; }
    public void deposit(double amount) { if (amount <= 0) throw new IllegalArgumentException("Deposit must be positive"); balance += amount; }
    public String accountNumber() { return accountNumber; }
    public String customerName() { return customerName; }
    public double balance() { return balance; }
    public boolean frozen() { return frozen; }
    public void freeze() { frozen = true; }
    public BehaviourProfile behaviourProfile() { return behaviourProfile; }
    public String pinHash() { return pinHash; }
    public String pinSalt() { return pinSalt; }
    public void setPinCredentials(String hash, String salt) { pinHash = hash; pinSalt = salt; }
    public String totpSecret() { return totpSecret; }
    public void setTotpSecret(String secret) { totpSecret = secret; }
    public Instant pinChangedAt() { return pinChangedAt; }
    public void markPinChanged() { pinChangedAt = Instant.now(); }
    public void unfreeze() { frozen = false; }
    /** Restores a persisted balance for this local classroom simulation. */
    public void restoreBalance(double storedBalance) {
        if (storedBalance < 0) throw new IllegalArgumentException("Stored balance cannot be negative");
        balance = storedBalance;
    }
}

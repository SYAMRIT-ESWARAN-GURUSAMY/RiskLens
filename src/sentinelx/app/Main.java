package sentinelx.app;

import sentinelx.model.SavingsAccount;
import sentinelx.model.Channel;
import sentinelx.model.TransactionType;
import sentinelx.exception.AuthenticationException;
import sentinelx.exception.BankingException;
import sentinelx.security.AuthService;
import sentinelx.security.TotpService;
import sentinelx.service.BankingService;
import sentinelx.service.EventMonitor;
import sentinelx.ui.SentinelXFrame;
import javax.swing.SwingUtilities;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/** Application entry point. */
public final class Main {
    private Main() { }
    public static void main(String[] args) {
        boolean demo = args.length > 0 && "--demo".equals(args[0]);
        BankingService bank = new BankingService(!demo);
        if (!demo) bank.loadSavedAccounts();
        addDemoAccountIfMissing(bank, "1001001", "Aarav Sharma", "1234", 75_000);
        addDemoAccountIfMissing(bank, "1001002", "Diya Patel", "5678", 42_000);
        EventMonitor monitor = new EventMonitor(transaction -> System.out.println("[monitor] " + transaction.type() + " received from " + transaction.channel()));
        bank.setEventMonitor(monitor);
        if (demo) {
            try { runConsoleDemo(bank); }
            finally { monitor.close(); }
            return;
        }
        System.out.println("SentinelX started. Demo account 1001001 is ready.");
        SwingUtilities.invokeLater(() -> {
            SentinelXFrame frame = new SentinelXFrame(bank);
            frame.addWindowListener(new WindowAdapter() {
                @Override public void windowClosed(WindowEvent event) { monitor.close(); }
            });
            frame.setVisible(true);
        });
    }
    private static void addDemoAccountIfMissing(BankingService bank, String number, String name, String pin, double balance) {
        if (bank.findAccount(number).isPresent()) return;
        SavingsAccount account = new SavingsAccount(number, name, balance);
        new AuthService().setPin(account, pin); account.setTotpSecret(TotpService.newSecret()); bank.register(account);
    }
    private static void runConsoleDemo(BankingService bank) {
        System.out.println("=== SentinelX full application demonstration ===");
        try {
            bank.authenticate("1001001", "1234");
            var normal = bank.transact("1001001", TransactionType.WITHDRAWAL, Channel.ATM, 1_000, "ATM-DELHI-01");
            System.out.println("Normal withdrawal: " + normal.decision() + " (risk " + normal.score() + ")");
            System.out.printf("Balance: Rs. %.2f%n", bank.findAccount("1001001").orElseThrow().balance());
            bank.transact("1001001", TransactionType.WITHDRAWAL, Channel.ATM, 50_000, "ATM-UNKNOWN-99");
        } catch (AuthenticationException | BankingException exception) {
            System.out.println("Security response: " + exception.getMessage().lines().findFirst().orElse("Rejected"));
        }
        System.out.println("Audit log: data/audit.log");
    }
}

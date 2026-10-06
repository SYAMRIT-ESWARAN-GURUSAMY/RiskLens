package sentinelx.simulator;

import sentinelx.exception.BankingException;
import sentinelx.model.*;
import sentinelx.service.BankingService;

/** Safe, local-only scenarios for demonstrating rule behavior during a viva. */
public final class AttackSimulator {
    private final BankingService bank;
    public AttackSimulator(BankingService bank) { this.bank = bank; }
    public String rapidWithdrawalDemo(String accountNumber) {
        StringBuilder report = new StringBuilder("Rapid withdrawal simulation:\n");
        for (int i = 1; i <= 4; i++) {
            try { var result = bank.transact(accountNumber, TransactionType.WITHDRAWAL, Channel.ATM, 1_000, "ATM-DEMO-" + i); report.append("Attempt ").append(i).append(": ").append(result.decision()).append('\n'); }
            catch (BankingException exception) { report.append("Attempt ").append(i).append(": ").append(exception.getMessage().lines().findFirst().orElse("Rejected")).append('\n'); }
        }
        return report.toString();
    }
    public String runScenario(String accountNumber, String scenario) {
        return switch (scenario) {
            case "Brute-force PIN" -> "Brute-force simulation is demonstrated by entering an incorrect PIN three times. The account locks and an administrator can safely unfreeze it.";
            case "Stolen card" -> tryTransaction(accountNumber, Channel.ATM, 20_000, "ATM-UNKNOWN-99");
            case "Cross-channel fraud" -> crossChannel(accountNumber);
            case "High-value withdrawal" -> tryTransaction(accountNumber, Channel.ATM, 50_000, "ATM-RISK-01");
            default -> rapidWithdrawalDemo(accountNumber);
        };
    }
    private String crossChannel(String accountNumber) {
        StringBuilder result = new StringBuilder("Cross-channel simulation:\n");
        result.append(tryTransaction(accountNumber, Channel.MOBILE, 500, "MOBILE-APP"));
        result.append(tryTransaction(accountNumber, Channel.INTERNET, 500, "WEB-PORTAL"));
        result.append(tryTransaction(accountNumber, Channel.POS, 3_000, "POS-UNKNOWN"));
        return result.toString();
    }
    private String tryTransaction(String accountNumber, Channel channel, double amount, String terminal) {
        try { var result = bank.transact(accountNumber, TransactionType.WITHDRAWAL, channel, amount, terminal); return channel + ": " + result.decision() + " (risk " + result.score() + ")\n" + result.explanation() + "\n"; }
        catch (BankingException exception) { return channel + ": " + exception.getMessage() + "\n"; }
    }
}

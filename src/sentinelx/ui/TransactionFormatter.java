package sentinelx.ui;

import sentinelx.model.Transaction;

/** Uses Java's pattern switch to format the transaction rows. */
public final class TransactionFormatter {
    private TransactionFormatter() { }
    public static String format(Object event) {
        return switch (event) {
            case Transaction(String id, String account, var type, var channel, double amount, String terminal, var time) ->
                    "%s | %s | %s | Rs. %.2f | %s".formatted(time.toLocalTime().withNano(0), type, channel, amount, terminal);
            default -> String.valueOf(event);
        };
    }
}

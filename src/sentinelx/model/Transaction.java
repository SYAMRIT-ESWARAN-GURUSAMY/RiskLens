package sentinelx.model;

import java.time.LocalDateTime;
import java.util.Objects;

/** Immutable transaction event. Java records reduce boilerplate safely. */
public record Transaction(String id, String accountNumber, TransactionType type,
                          Channel channel, double amount, String terminalId,
                          LocalDateTime occurredAt) {
    public Transaction {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(accountNumber, "accountNumber");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(channel, "channel");
        Objects.requireNonNull(terminalId, "terminalId");
        Objects.requireNonNull(occurredAt, "occurredAt");
        if (amount < 0) throw new IllegalArgumentException("Amount cannot be negative");
    }
}

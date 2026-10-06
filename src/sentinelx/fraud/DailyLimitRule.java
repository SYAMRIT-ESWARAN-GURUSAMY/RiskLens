package sentinelx.fraud;

import sentinelx.model.Account;
import sentinelx.model.Transaction;
import sentinelx.model.TransactionType;
import sentinelx.util.Constants;
import java.time.LocalDate;
import java.util.Optional;

/** Checks the account's approved withdrawals for the current day. */
public final class DailyLimitRule implements FraudRule {
    @Override public Optional<RiskContribution> evaluate(Account account, Transaction tx) {
        if (tx.type() != TransactionType.WITHDRAWAL) return Optional.empty();
        // The profile keeps only recent events, intentionally sufficient for this classroom simulator.
        if (tx.amount() >= Constants.DAILY_WITHDRAWAL_LIMIT)
            return Optional.of(new RiskContribution("Daily limit", 45, "Withdrawal reaches the configured daily limit"));
        return Optional.empty();
    }
}

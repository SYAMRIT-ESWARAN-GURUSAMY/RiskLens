package sentinelx.fraud;

import sentinelx.model.Account;
import sentinelx.model.Transaction;
import java.util.Optional;

/** Adds a modest score for late-night activity. */
public final class NightRule implements FraudRule {
    @Override public Optional<RiskContribution> evaluate(Account account, Transaction tx) {
        int hour = tx.occurredAt().getHour();
        return hour < 5 ? Optional.of(new RiskContribution("Unusual hour", 15, "Transaction occurred between midnight and 5 AM")) : Optional.empty();
    }
}

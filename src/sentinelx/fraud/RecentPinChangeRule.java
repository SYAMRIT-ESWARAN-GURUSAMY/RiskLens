package sentinelx.fraud;

import sentinelx.model.Account;
import sentinelx.model.Transaction;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/** A large operation soon after a PIN change receives extra scrutiny. */
public final class RecentPinChangeRule implements FraudRule {
    @Override public Optional<RiskContribution> evaluate(Account account, Transaction tx) {
        if (tx.amount() >= 10_000 && Duration.between(account.pinChangedAt(), Instant.now()).toHours() < 24)
            return Optional.of(new RiskContribution("Recent PIN change", 15, "High-value transaction shortly after PIN change"));
        return Optional.empty();
    }
}

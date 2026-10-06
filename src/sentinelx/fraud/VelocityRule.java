package sentinelx.fraud;

import sentinelx.model.Account;
import sentinelx.model.Transaction;
import java.time.LocalDateTime;
import java.util.Optional;

/** Detects a burst of activity within five minutes. */
public final class VelocityRule implements FraudRule {
    @Override public Optional<RiskContribution> evaluate(Account account, Transaction tx) {
        if (account.behaviourProfile().recentCountSince(LocalDateTime.now().minusMinutes(5)) >= 3)
            return Optional.of(new RiskContribution("Transaction velocity", 40, "Several transactions occurred within five minutes"));
        return Optional.empty();
    }
}

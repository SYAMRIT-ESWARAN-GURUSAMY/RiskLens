package sentinelx.fraud;

import sentinelx.model.Account;
import sentinelx.model.Transaction;
import java.util.Optional;

/** Flags abrupt movement between several simulated banking channels. */
public final class CrossChannelRule implements FraudRule {
    @Override public Optional<RiskContribution> evaluate(Account account, Transaction tx) {
        if (account.behaviourProfile().distinctRecentChannels() >= 2 && account.behaviourProfile().useCount(tx.channel()) == 0)
            return Optional.of(new RiskContribution("Cross-channel activity", 20, "New channel used after recent activity on other channels"));
        return Optional.empty();
    }
}

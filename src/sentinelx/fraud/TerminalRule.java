package sentinelx.fraud;

import sentinelx.model.Account;
import sentinelx.model.Transaction;
import java.util.Optional;

/** Detects transactions from previously unseen terminal identifiers. */
public final class TerminalRule implements FraudRule {
    @Override public Optional<RiskContribution> evaluate(Account account, Transaction tx) {
        if (!account.behaviourProfile().familiarTerminals().isEmpty() && !account.behaviourProfile().isFamiliarTerminal(tx.terminalId()))
            return Optional.of(new RiskContribution("Unknown terminal", 20, "Terminal has not been used by this account before"));
        return Optional.empty();
    }
}

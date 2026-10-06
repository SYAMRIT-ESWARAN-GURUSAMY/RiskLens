package sentinelx.fraud;

import sentinelx.model.Account;
import sentinelx.model.Transaction;
import sentinelx.model.TransactionType;
import java.util.Optional;

/** Flags withdrawals substantially above an account's observed norm. */
public final class AmountRule implements FraudRule {
    @Override public Optional<RiskContribution> evaluate(Account account, Transaction tx) {
        double average = account.behaviourProfile().averageWithdrawal();
        if (tx.type() == TransactionType.WITHDRAWAL && tx.amount() >= 10_000 && (average == 0 || tx.amount() > average * 3))
            return Optional.of(new RiskContribution("Amount anomaly", 35, "Withdrawal is unusually high for this profile"));
        return Optional.empty();
    }
}

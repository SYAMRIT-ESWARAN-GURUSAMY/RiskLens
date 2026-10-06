package sentinelx.fraud;

import sentinelx.model.Account;
import sentinelx.model.Transaction;
import java.util.Optional;

/** Strategy interface; each implementation assesses one suspicious pattern. */
public interface FraudRule { Optional<RiskContribution> evaluate(Account account, Transaction transaction); }

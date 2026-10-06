package sentinelx.fraud;

import sentinelx.model.Account;
import sentinelx.model.Decision;
import sentinelx.model.Transaction;
import sentinelx.util.Constants;
import java.util.ArrayList;
import java.util.List;

/** Coordinates polymorphic rules and chooses a transparent outcome. */
public final class FraudEngine {
    private final List<FraudRule> rules = List.of(new AmountRule(), new TerminalRule(), new VelocityRule(), new NightRule(), new DailyLimitRule(), new CrossChannelRule(), new RecentPinChangeRule());
    public RiskAssessment assess(Account account, Transaction transaction) {
        List<RiskContribution> findings = new ArrayList<>();
        for (FraudRule rule : rules) rule.evaluate(account, transaction).ifPresent(findings::add);
        int score = findings.stream().mapToInt(RiskContribution::points).sum();
        Decision decision = score >= Constants.HIGH_RISK_THRESHOLD ? Decision.FREEZE : score >= Constants.MONITOR_THRESHOLD ? Decision.MONITOR : score >= Constants.CHALLENGE_THRESHOLD ? Decision.CHALLENGE : Decision.ALLOW;
        return new RiskAssessment(score, decision, List.copyOf(findings));
    }
}

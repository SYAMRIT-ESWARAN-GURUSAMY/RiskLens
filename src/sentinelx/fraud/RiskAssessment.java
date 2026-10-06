package sentinelx.fraud;

import sentinelx.model.Decision;
import java.util.List;

/** Immutable result returned by the fraud engine. */
public record RiskAssessment(int score, Decision decision, List<RiskContribution> contributions) {
    public String explanation() {
        StringBuilder text = new StringBuilder("Risk score: ").append(score).append(" | Decision: ").append(decision);
        contributions.forEach(item -> text.append(System.lineSeparator()).append("- ").append(item.reason()).append(" (+").append(item.points()).append(')'));
        return text.toString();
    }
}

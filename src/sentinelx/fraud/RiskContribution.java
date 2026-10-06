package sentinelx.fraud;

/** A single transparent contribution to a fraud risk total. */
public record RiskContribution(String rule, int points, String reason) { }

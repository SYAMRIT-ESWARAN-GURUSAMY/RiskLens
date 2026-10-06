package sentinelx.util;

/** Central constants shared by the simulator. */
public final class Constants {
    public static final double MINIMUM_SAVINGS_BALANCE = 500.00;
    public static final int MAX_PIN_ATTEMPTS = 3;
    public static final int HIGH_RISK_THRESHOLD = 90;
    public static final int MONITOR_THRESHOLD = 61;
    public static final int CHALLENGE_THRESHOLD = 31;
    public static final int SESSION_TIMEOUT_MINUTES = 5;
    public static final double DAILY_WITHDRAWAL_LIMIT = 40_000.00;
    public static final String ADMIN_ACCESS_CODE = "admin123";

    private Constants() { }
}

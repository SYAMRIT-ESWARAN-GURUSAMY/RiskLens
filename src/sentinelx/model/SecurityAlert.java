package sentinelx.model;

import java.time.LocalDateTime;

/** Immutable alert shown in the administrator dashboard. */
public record SecurityAlert(LocalDateTime createdAt, AlertSeverity severity, String accountNumber,
                            String title, String details) { }

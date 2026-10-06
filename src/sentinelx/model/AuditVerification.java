package sentinelx.model;

/** Result of checking the local tamper-evident audit chain. */
public record AuditVerification(boolean valid, int recordsChecked, String message) { }

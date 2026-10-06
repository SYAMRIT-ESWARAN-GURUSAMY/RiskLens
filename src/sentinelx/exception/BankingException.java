package sentinelx.exception;

/** Raised when a banking operation cannot be completed. */
public class BankingException extends Exception { public BankingException(String message) { super(message); } }

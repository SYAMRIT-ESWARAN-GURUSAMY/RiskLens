package sentinelx.exception;

/** Raised when an account cannot be authenticated. */
public class AuthenticationException extends Exception { public AuthenticationException(String message) { super(message); } }

package sentinelx.model;

import java.time.Instant;

/** Authenticated session with a short inactivity timeout. */
public record Session(String token, String accountNumber, Instant expiresAt) {
    public boolean active() { return Instant.now().isBefore(expiresAt); }
}

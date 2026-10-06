package sentinelx.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** Salted SHA-256 helper for an educational local simulation. */
public final class PinHasher {
    private static final SecureRandom RANDOM = new SecureRandom();
    private PinHasher() { }
    public static String newSalt() { byte[] bytes = new byte[16]; RANDOM.nextBytes(bytes); return Base64.getEncoder().encodeToString(bytes); }
    public static String hash(String pin, String salt) {
        try { return Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest((salt + ':' + pin).getBytes(StandardCharsets.UTF_8))); }
        catch (Exception exception) { throw new IllegalStateException("SHA-256 is unavailable", exception); }
    }
    public static boolean matches(String pin, String expectedHash, String salt) {
        return MessageDigest.isEqual(hash(pin, salt).getBytes(StandardCharsets.UTF_8), expectedHash.getBytes(StandardCharsets.UTF_8));
    }
}

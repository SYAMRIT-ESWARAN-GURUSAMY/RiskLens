package sentinelx.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

/** Minimal RFC-6238-style six digit TOTP implementation for the local simulator. */
public final class TotpService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private TotpService() { }
    public static String newSecret() { byte[] bytes = new byte[20]; RANDOM.nextBytes(bytes); return Base64.getEncoder().encodeToString(bytes); }
    public static String currentCode(String secret) { return codeAt(secret, Instant.now().getEpochSecond() / 30); }
    public static boolean matches(String secret, String code) {
        if (code == null || !code.matches("\\d{6}")) return false;
        long step = Instant.now().getEpochSecond() / 30;
        return currentCode(secret).equals(code) || codeAt(secret, step - 1).equals(code);
    }
    private static String codeAt(String secret, long counter) {
        try {
            byte[] key = Base64.getDecoder().decode(secret);
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] result = mac.doFinal(ByteBuffer.allocate(8).putLong(counter).array());
            int offset = result[result.length - 1] & 0x0f;
            int value = ((result[offset] & 0x7f) << 24) | ((result[offset + 1] & 0xff) << 16) | ((result[offset + 2] & 0xff) << 8) | (result[offset + 3] & 0xff);
            return "%06d".formatted(value % 1_000_000);
        } catch (Exception exception) { throw new IllegalStateException("Could not calculate verification code", exception); }
    }
}

package sentinelx.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import sentinelx.model.AuditVerification;

/** Appends local audit records linked to the hash of the previous record. */
public final class AuditLogger {
    private final Path file = Path.of("data", "audit.log");
    private String previousHash = "GENESIS";
    public AuditLogger() {
        try { Files.createDirectories(file.getParent()); if (Files.exists(file)) previousHash = Files.readAllLines(file).stream().reduce((a, b) -> b).orElse("GENESIS").replaceFirst(".*hash=", ""); }
        catch (IOException ignored) { }
    }
    public synchronized void log(String action, String details) {
        String raw = LocalDateTime.now() + " | " + action + " | " + details + " | previous=" + previousHash;
        previousHash = sha256(raw);
        try { Files.writeString(file, raw + " | hash=" + previousHash + System.lineSeparator(), StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND); }
        catch (IOException exception) { throw new IllegalStateException("Could not write audit log", exception); }
    }
    /** Verifies that each record's stored previous hash links to the prior record. */
    public synchronized AuditVerification verify() {
        try {
            if (!Files.exists(file)) return new AuditVerification(true, 0, "No audit records exist yet");
            String expectedPrevious = "GENESIS"; int checked = 0;
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                int previousIndex = line.lastIndexOf(" | previous=");
                int hashIndex = line.lastIndexOf(" | hash=");
                if (previousIndex < 0 || hashIndex < 0) return new AuditVerification(false, checked, "Malformed audit record");
                String raw = line.substring(0, hashIndex);
                String previous = line.substring(previousIndex + " | previous=".length(), hashIndex);
                String hash = line.substring(hashIndex + " | hash=".length());
                if (!previous.equals(expectedPrevious) || !sha256(raw).equals(hash)) return new AuditVerification(false, checked, "Hash chain failed at record " + (checked + 1));
                expectedPrevious = hash; checked++;
            }
            return new AuditVerification(true, checked, "Audit chain is valid");
        } catch (IOException exception) { return new AuditVerification(false, 0, "Could not read audit log: " + exception.getMessage()); }
    }
    private String sha256(String value) {
        try { byte[] bytes = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)); StringBuilder output = new StringBuilder(); for (byte b : bytes) output.append(String.format("%02x", b)); return output.toString(); }
        catch (Exception exception) { throw new IllegalStateException(exception); }
    }
}

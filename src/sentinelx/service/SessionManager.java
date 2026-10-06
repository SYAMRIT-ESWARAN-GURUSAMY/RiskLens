package sentinelx.service;

import sentinelx.model.Session;
import sentinelx.util.Constants;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory session registry used by ATM and simulated channels. */
public final class SessionManager {
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    public Session open(String accountNumber) {
        Session session = new Session(UUID.randomUUID().toString(), accountNumber, Instant.now().plusSeconds(Constants.SESSION_TIMEOUT_MINUTES * 60L));
        sessions.put(session.token(), session); return session;
    }
    public Optional<Session> valid(String token) {
        Session session = sessions.get(token);
        if (session == null || !session.active()) { sessions.remove(token); return Optional.empty(); }
        return Optional.of(session);
    }
    public void close(String token) { sessions.remove(token); }
    public int activeCount() { sessions.entrySet().removeIf(entry -> !entry.getValue().active()); return sessions.size(); }
}

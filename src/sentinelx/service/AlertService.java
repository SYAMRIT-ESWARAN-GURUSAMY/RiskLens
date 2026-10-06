package sentinelx.service;

import sentinelx.model.SecurityAlert;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Observer-style alert store consumed by the administrator dashboard. */
public final class AlertService {
    private final List<SecurityAlert> alerts = new CopyOnWriteArrayList<>();
    public void publish(SecurityAlert alert) { alerts.add(0, alert); }
    public List<SecurityAlert> all() { return List.copyOf(alerts); }
    public void clear() { alerts.clear(); }
}

package sentinelx.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.EnumMap;
import java.util.Map;

/** Learns simple running averages and familiar terminals for one account. */
public final class BehaviourProfile {
    private double averageWithdrawal;
    private int withdrawalCount;
    private final List<String> familiarTerminals = new ArrayList<>();
    private final List<LocalDateTime> recentTransactions = new ArrayList<>();
    private final List<Channel> recentChannels = new ArrayList<>();
    private final Map<Channel, Integer> channelUse = new EnumMap<>(Channel.class);

    public void learn(Transaction transaction) {
        recentTransactions.add(transaction.occurredAt());
        recentChannels.add(transaction.channel());
        channelUse.merge(transaction.channel(), 1, Integer::sum);
        if (recentTransactions.size() > 20) recentTransactions.remove(0);
        if (recentChannels.size() > 20) recentChannels.remove(0);
        if (!familiarTerminals.contains(transaction.terminalId())) familiarTerminals.add(transaction.terminalId());
        if (transaction.type() == TransactionType.WITHDRAWAL) {
            averageWithdrawal = ((averageWithdrawal * withdrawalCount) + transaction.amount()) / ++withdrawalCount;
        }
    }

    public boolean isFamiliarTerminal(String terminalId) { return familiarTerminals.contains(terminalId); }
    public double averageWithdrawal() { return averageWithdrawal; }
    public int recentCountSince(LocalDateTime cutoff) {
        return (int) recentTransactions.stream().filter(time -> time.isAfter(cutoff)).count();
    }
    public List<String> familiarTerminals() { return List.copyOf(familiarTerminals); }
    public int distinctRecentChannels() { return (int) recentChannels.stream().distinct().count(); }
    public int useCount(Channel channel) { return channelUse.getOrDefault(channel, 0); }
}

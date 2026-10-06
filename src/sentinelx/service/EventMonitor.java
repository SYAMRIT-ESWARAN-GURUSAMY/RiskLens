package sentinelx.service;

import sentinelx.model.Transaction;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

/**
 * A small producer-consumer monitor. Transactions are published by the banking
 * service and observed on a Java 21 virtual thread without blocking the Swing UI.
 */
public final class EventMonitor implements AutoCloseable {
    private final BlockingQueue<Transaction> events = new LinkedBlockingQueue<>();
    private final Consumer<Transaction> consumer;
    private volatile boolean running = true;
    private final Thread monitorThread;

    public EventMonitor(Consumer<Transaction> consumer) {
        this.consumer = consumer;
        monitorThread = Thread.startVirtualThread(this::drainEvents);
    }
    public void publish(Transaction transaction) { events.offer(transaction); }
    private void drainEvents() {
        while (running) {
            try { consumer.accept(events.take()); }
            catch (InterruptedException exception) { Thread.currentThread().interrupt(); return; }
        }
    }
    @Override public void close() { running = false; monitorThread.interrupt(); }
}

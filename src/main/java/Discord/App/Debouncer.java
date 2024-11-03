package Discord.App;

import java.util.concurrent.*;

/**
 * <p>Debouncer class.</p>
 *
 * @author xXTheSebXx
 * @version 1.0-SNAPSHOT
 */
public class Debouncer {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ConcurrentHashMap<Object, Future<?>> delayedMap = new ConcurrentHashMap<>();

    /**
     * Debounces {@code callable} by {@code delay}, i.e., schedules it to be executed after {@code delay},
     * or cancels its execution if the method is called with the same key within the {@code delay} again.
     *
     * @param key a {@link java.lang.Object} object
     * @param runnable a {@link java.lang.Runnable} object
     * @param delay a long
     * @param unit a {@link java.util.concurrent.TimeUnit} object
     */
    public void debounce(final Object key, final Runnable runnable, long delay, TimeUnit unit) {
        final Future<?> prev = delayedMap.put(key, scheduler.schedule(() -> {
            try {
                runnable.run();
            } finally {
                delayedMap.remove(key);
            }
        }, delay, unit));
        if (prev != null) {
            prev.cancel(true);
        }
    }

    /**
     * <p>shutdown.</p>
     */
    public void shutdown() {
        scheduler.shutdownNow();
    }
}
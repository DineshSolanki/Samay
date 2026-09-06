package io.github.dineshsolanki.samay;

/**
 * Thread-local holder for timezone context. Supports both regular and inheritable
 * thread-local storage, configurable at construction time.
 *
 * <p>The holder is lazily initialized on first access. Call {@link #initialize(boolean)}
 * before any usage to control inheritable mode; defaults to non-inheritable.</p>
 *
 * @param <T> the type of timezone value stored (typically {@link java.time.ZoneId})
 */
public class TimezoneContextHolder<T> {

    private static volatile boolean initialized = false;
    private static volatile boolean useInheritable = false;

    private final ThreadLocal<T> timezoneThreadLocal;

    /**
     * Initialize the global inheritable mode. Must be called before any timezone
     * operations. Safe to call multiple times — only the first call takes effect.
     *
     * @param inheritable whether child threads should inherit the timezone context
     */
    public static void initialize(boolean inheritable) {
        if (!initialized) {
            synchronized (TimezoneContextHolder.class) {
                if (!initialized) {
                    useInheritable = inheritable;
                    initialized = true;
                }
            }
        }
    }

    /**
     * Reset initialization state. Used for testing only.
     */
    static void resetForTesting() {
        initialized = false;
        useInheritable = false;
    }

    public TimezoneContextHolder(boolean useInheritable) {
        if (useInheritable) {
            this.timezoneThreadLocal = new InheritableThreadLocal<>();
        } else {
            this.timezoneThreadLocal = new ThreadLocal<>();
        }
    }

    public void setTimezone(T timezone) {
        timezoneThreadLocal.set(timezone);
    }

    public T getTimezone() {
        return timezoneThreadLocal.get();
    }

    public void clear() {
        timezoneThreadLocal.remove();
    }
}

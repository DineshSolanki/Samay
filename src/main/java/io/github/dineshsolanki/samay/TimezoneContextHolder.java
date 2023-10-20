package io.github.dineshsolanki.samay;

public class TimezoneContextHolder<T> {
    private final ThreadLocal<T> timezoneThreadLocal;

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

package io.github.dineshsolanki.samay;

import io.github.dineshsolanki.samay.strategy.TimezoneResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * Spring Boot interceptor that resolves and propagates the client's timezone
 * across the entire request lifecycle.
 *
 * <p>Provides a static API for timezone-aware timestamp operations:</p>
 * <ul>
 *   <li>{@link #getZoneId()} — get the current request's {@link ZoneId}</li>
 *   <li>{@link #now()} — current moment in the user's timezone</li>
 *   <li>{@link #atZone(Instant)} — convert any {@link Instant} to the user's timezone</li>
 *   <li>{@link #format(Instant, String)} — format timestamps in the user's timezone</li>
 * </ul>
 *
 * <p>Timezone resolution is pluggable via {@link TimezoneResolver} (header, JWT, fixed, etc.).</p>
 */
public class Samay implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(Samay.class);

    // Lazy-initialized holder; respects the inheritable config set via initialize() or constructor
    private static volatile TimezoneContextHolder<ZoneId> zoneHolder;

    // Pre-configured inheritable setting; set by autoconfiguration or initialize()
    // before the holder is created. Defaults to false.
    private static volatile boolean pendingInheritable = false;

    private final TimezoneResolver resolver;

    /**
     * @param resolver       the strategy to resolve timezone from each request
     * @param useInheritable whether to use InheritableThreadLocal for child thread propagation
     */
    public Samay(TimezoneResolver resolver, boolean useInheritable) {
        this.resolver = resolver;
        initialize(useInheritable);
    }

    /**
     * Initialize the timezone holder with the given inheritable mode.
     * Safe to call multiple times — only the first call takes effect.
     * Called automatically by the constructor.
     *
     * <p>Also sets the pending inheritable value so that {@link #ensureInitialized()}
     * respects the configured mode even if called before the Spring context starts.</p>
     */
    public static void initialize(boolean useInheritable) {
        pendingInheritable = useInheritable;
        if (zoneHolder == null) {
            synchronized (Samay.class) {
                if (zoneHolder == null) {
                    zoneHolder = new TimezoneContextHolder<>(useInheritable);
                    logger.debug("Samay: initialized with inheritable={}", useInheritable);
                }
            }
        }
    }

    // ─── Interceptor lifecycle ──────────────────────────────────────────

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        ZoneId zone = resolver.resolve(request);
        if (zone == null) {
            zone = ZoneId.systemDefault();
            logger.debug("Samay: no timezone resolved, using system default '{}'", zone);
        }
        setZoneId(zone);
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                Exception ex) {
        clear();
    }

    // ─── Static API — ZoneId access ─────────────────────────────────────

    /**
     * Get the current request's timezone as a {@link ZoneId}.
     *
     * @return the current zone, or the system default if not in a request context
     */
    public static ZoneId getZoneId() {
        ensureInitialized();
        ZoneId zone = zoneHolder.getTimezone();
        return zone != null ? zone : ZoneId.systemDefault();
    }

    /**
     * Set the timezone for the current thread. Primarily used by the interceptor
     * and async propagation; prefer using the interceptor for request-scoped timezone.
     *
     * @param zoneId the timezone to set
     */
    public static void setZoneId(ZoneId zoneId) {
        ensureInitialized();
        zoneHolder.setTimezone(zoneId);
    }

    /**
     * Check whether a timezone has been explicitly set for the current thread
     * (as opposed to falling back to system default).
     */
    public static boolean isSet() {
        ensureInitialized();
        return zoneHolder.getTimezone() != null;
    }

    /**
     * Clear the timezone context for the current thread.
     * Called automatically after request completion.
     */
    public static void clear() {
        ensureInitialized();
        zoneHolder.clear();
    }

    private static void ensureInitialized() {
        if (zoneHolder == null) {
            initialize(pendingInheritable);
        }
    }

    // ─── Static API — Convenience timestamp methods ─────────────────────

    /**
     * Current moment as a {@link ZonedDateTime} in the user's timezone.
     *
     * <pre>{@code
     * ZonedDateTime userNow = Samay.now();
     * // e.g. 2026-09-06T15:30:00-04:00[America/New_York]
     * }</pre>
     */
    public static ZonedDateTime now() {
        return Instant.now().atZone(getZoneId());
    }

    /**
     * Convert an {@link Instant} to the user's timezone.
     *
     * <pre>{@code
     * Instant stored = entity.getCreatedAt(); // UTC from DB
     * ZonedDateTime userTime = Samay.atZone(stored);
     * }</pre>
     */
    public static ZonedDateTime atZone(Instant instant) {
        if (instant == null) return null;
        return instant.atZone(getZoneId());
    }

    /**
     * Interpret a {@link LocalDateTime} as being in the user's timezone.
     *
     * @param dateTime a local date/time to treat as the user's local time
     * @return the corresponding {@link ZonedDateTime}
     */
    public static ZonedDateTime atZone(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.atZone(getZoneId());
    }

    /**
     * Convert a {@link LocalDate} to a {@link ZonedDateTime} at start of day in the user's timezone.
     */
    public static ZonedDateTime atZone(LocalDate date) {
        if (date == null) return null;
        return date.atStartOfDay(getZoneId());
    }

    /**
     * Format an {@link Instant} in the user's timezone using the given pattern.
     *
     * <pre>{@code
     * String display = Samay.format(entity.getCreatedAt(), "MMM dd, yyyy HH:mm");
     * // "Sep 06, 2026 15:30" (if user is in America/New_York)
     * }</pre>
     *
     * @param instant the instant to format
     * @param pattern {@link DateTimeFormatter} pattern
     * @return formatted string, or {@code null} if instant is null
     */
    public static String format(Instant instant, String pattern) {
        if (instant == null) return null;
        return atZone(instant).format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Format a {@link ZonedDateTime} using the given pattern.
     */
    public static String format(ZonedDateTime dateTime, String pattern) {
        if (dateTime == null) return null;
        return dateTime.withZoneSameInstant(getZoneId())
                .format(DateTimeFormatter.ofPattern(pattern));
    }

    // ─── Static API — Date-range helpers ────────────────────────────────

    /**
     * Get the start of a date in the user's timezone as a UTC {@link Instant}.
     * Useful for date-range queries against UTC-stored timestamps.
     *
     * <pre>{@code
     * Instant start = Samay.startOfDay(LocalDate.of(2026, 9, 6));
     * Instant end = Samay.endOfDay(LocalDate.of(2026, 9, 6));
     * // SELECT * FROM events WHERE created_at BETWEEN :start AND :end
     * }</pre>
     */
    public static Instant startOfDay(LocalDate date) {
        if (date == null) return null;
        return date.atStartOfDay(getZoneId()).toInstant();
    }

    /**
     * Get the end of a date (23:59:59.999999999) in the user's timezone as a UTC {@link Instant}.
     */
    public static Instant endOfDay(LocalDate date) {
        if (date == null) return null;
        return date.plusDays(1).atStartOfDay(getZoneId()).toInstant().minusNanos(1);
    }

    // ─── Legacy API (deprecated) ────────────────────────────────────────

    /**
     * @deprecated Use {@link #getZoneId()} instead. Retained for backward compatibility.
     */
    @Deprecated(since = "4.0", forRemoval = true)
    public static TimeZone getTimeZone() {
        return TimeZone.getTimeZone(getZoneId());
    }
}

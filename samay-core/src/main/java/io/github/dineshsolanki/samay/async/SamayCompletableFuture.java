package io.github.dineshsolanki.samay.async;

import io.github.dineshsolanki.samay.Samay;

import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Timezone-aware wrappers around {@link CompletableFuture}.
 *
 * <p>These methods capture the current thread's timezone and restore it
 * in the async worker thread, so {@link Samay#getZoneId()} works correctly
 * inside the submitted task:</p>
 *
 * <pre>{@code
 * // The timezone is propagated into the supplyAsync task
 * SamayCompletableFuture.supplyAsync(() -> {
 *     // Samay.getZoneId() returns the caller's timezone here
 *     return service.findEvents(Samay.now());
 * });
 * }</pre>
 *
 * <p><b>Important:</b> The timezone is captured at the point of the {@code supplyAsync}/{@code runAsync}
 * call. Standard {@link CompletableFuture} chaining methods ({@code thenApply}, {@code thenCompose}, etc.)
 * do NOT propagate the timezone — they run in whatever thread completes the future. For chained async
 * operations, use {@link #applyAsync} or wrap each stage with its own {@code supplyAsync}/{@code runAsync}.</p>
 *
 * <pre>{@code
 * // For chained async operations, capture the zone in each stage:
 * ZoneId zone = Samay.getZoneId();
 * SamayCompletableFuture.supplyAsync(() -> {
 *     return service.findEvents(Samay.now());
 * }).thenCompose(events -> {
 *     // Need to re-capture if running in a different thread
 *     Samay.setZoneId(zone);
 *     try {
 *         return SamayCompletableFuture.supplyAsync(() -> enrichEvents(events));
 *     } finally {
 *         Samay.clear();
 *     }
 * });
 * }</pre>
 */
public final class SamayCompletableFuture {

    private SamayCompletableFuture() {}

    /**
     * {@link CompletableFuture#supplyAsync(Supplier)} with timezone propagation.
     */
    public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier) {
        ZoneId zone = captureZone();
        return CompletableFuture.supplyAsync(withZone(supplier, zone));
    }

    /**
     * {@link CompletableFuture#supplyAsync(Supplier, Executor)} with timezone propagation.
     */
    public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor) {
        ZoneId zone = captureZone();
        return CompletableFuture.supplyAsync(withZone(supplier, zone), executor);
    }

    /**
     * {@link CompletableFuture#runAsync(Runnable)} with timezone propagation.
     */
    public static CompletableFuture<Void> runAsync(Runnable runnable) {
        ZoneId zone = captureZone();
        return CompletableFuture.runAsync(withZone(runnable, zone));
    }

    /**
     * {@link CompletableFuture#runAsync(Runnable, Executor)} with timezone propagation.
     */
    public static CompletableFuture<Void> runAsync(Runnable runnable, Executor executor) {
        ZoneId zone = captureZone();
        return CompletableFuture.runAsync(withZone(runnable, zone), executor);
    }

    /**
     * Chain a {@code thenApplyAsync} that preserves timezone context.
     */
    public static <T, U> Function<T, CompletableFuture<U>> applyAsync(
            Function<T, U> fn, Executor executor) {
        ZoneId zone = captureZone();
        return value -> CompletableFuture.supplyAsync(() -> {
            try {
                Samay.setZoneId(zone);
                return fn.apply(value);
            } finally {
                Samay.clear();
            }
        }, executor);
    }

    // ─── Internal helpers ───────────────────────────────────────────────

    private static ZoneId captureZone() {
        return Samay.isSet() ? Samay.getZoneId() : null;
    }

    private static <U> Supplier<U> withZone(Supplier<U> supplier, ZoneId zone) {
        return () -> {
            try {
                if (zone != null) Samay.setZoneId(zone);
                return supplier.get();
            } finally {
                Samay.clear();
            }
        };
    }

    private static Runnable withZone(Runnable runnable, ZoneId zone) {
        return () -> {
            try {
                if (zone != null) Samay.setZoneId(zone);
                runnable.run();
            } finally {
                Samay.clear();
            }
        };
    }
}

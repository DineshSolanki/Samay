package io.github.dineshsolanki.samay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.ZoneId;

/**
 * Reactive {@link WebFilter} for Spring WebFlux that resolves and propagates
 * the client's timezone across the reactive pipeline.
 *
 * <p>Stores the timezone in Reactor's {@link reactor.util.context.Context} so it
 * propagates correctly regardless of which thread the handler executes on.
 * Also sets the {@link Samay} ThreadLocal at subscription time so that
 * {@link Samay#getZoneId()} works in both imperative and reactive code paths.</p>
 *
 * <p>Registered automatically by auto-configuration when WebFlux is on the classpath.
 * The timezone is resolved using the configured {@code samay.strategy} and
 * {@code samay.header-name} properties (header strategy is used for WebFlux;
 * JWT strategy falls back to header for reactive contexts).</p>
 *
 * <p>For manual registration:</p>
 * <pre>{@code
 * @Bean
 * public SamayWebFilter samayWebFilter(SamayProperties properties) {
 *     return new SamayWebFilter(properties);
 * }
 * }</pre>
 */
public class SamayWebFilter implements WebFilter {

    private static final Logger logger = LoggerFactory.getLogger(SamayWebFilter.class);

    /**
     * Reactor Context key for the resolved {@link ZoneId}.
     * Access in reactive pipelines via {@code Mono.deferContextual(ctx -> ctx.get(SamayWebFilter.CONTEXT_KEY))}.
     */
    public static final String CONTEXT_KEY = "samay.zone";

    private final String headerName;
    private final ZoneId fixedZone;
    private final boolean useFixed;

    /**
     * Create from SamayProperties. Uses header strategy (falls back to header for JWT in reactive).
     */
    public SamayWebFilter(SamayProperties properties) {
        this.headerName = properties.getHeaderName();
        this.fixedZone = properties.getStrategy() == ZoneStrategy.FIXED
                ? ZoneId.of(properties.getFixedZone()) : null;
        this.useFixed = properties.getStrategy() == ZoneStrategy.FIXED;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ZoneId zone = resolveZone(exchange);
        if (zone == null) {
            zone = ZoneId.systemDefault();
            logger.debug("Samay: no timezone resolved, using system default '{}'", zone);
        }

        ZoneId resolvedZone = zone;

        return chain.filter(exchange)
                // Store zone in Reactor Context — propagates correctly through any thread
                .contextWrite(ctx -> ctx.put(CONTEXT_KEY, resolvedZone))
                // Set ThreadLocal at subscription time so Samay.getZoneId() works in handlers
                .transformDeferred(delegate -> Mono.deferContextual(ctx -> {
                    ZoneId ctxZone = ctx.getOrDefault(CONTEXT_KEY, resolvedZone);
                    Samay.setZoneId(ctxZone);
                    return delegate.doFinally(signal -> Samay.clear());
                }));
    }

    private ZoneId resolveZone(ServerWebExchange exchange) {
        if (useFixed) {
            return fixedZone;
        }
        String headerValue = exchange.getRequest().getHeaders().getFirst(headerName);
        if (headerValue == null || headerValue.isBlank()) {
            return null;
        }
        try {
            return ZoneId.of(headerValue);
        } catch (Exception e) {
            logger.warn("Samay: invalid timezone '{}' in reactive header '{}', ignoring",
                    headerValue, headerName);
            return null;
        }
    }
}

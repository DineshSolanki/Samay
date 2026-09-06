package io.github.dineshsolanki.samay;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.DateTimeException;
import java.time.ZoneId;

/**
 * Configuration properties for the Samay timezone library.
 *
 * <p>Set these in {@code application.properties} or {@code application.yml} under the {@code samay.} prefix.</p>
 *
 * <p>Validates on startup: {@code fixedZone} must be a valid {@link ZoneId} when strategy is FIXED,
 * and {@code headerName}/{@code jwtClaim} must not be blank.</p>
 */
@ConfigurationProperties(prefix = "samay")
public class SamayProperties {

    /**
     * Whether Samay is enabled. Set to false to disable all timezone handling.
     */
    private boolean enabled = true;

    /**
     * The HTTP header name to read the timezone from (when using HEADER strategy).
     */
    private String headerName = "X-TimeZone";

    /**
     * Whether timezone context propagates to child threads via InheritableThreadLocal.
     */
    private boolean threadInheritable = false;

    /**
     * The timezone resolution strategy: HEADER, JWT, or FIXED.
     */
    private ZoneStrategy strategy = ZoneStrategy.HEADER;

    /**
     * The JWT claim name containing the timezone (when using JWT strategy).
     * The claim value should be a valid IANA timezone ID (e.g. "America/New_York").
     */
    private String jwtClaim = "timezone";

    /**
     * The fixed timezone ID to use (when using FIXED strategy).
     * Must be a valid {@link ZoneId} identifier.
     */
    private String fixedZone = "UTC";

    /**
     * Validate properties after binding. Throws a clear error if configuration is invalid.
     */
    @PostConstruct
    public void validate() {
        if (headerName == null || headerName.isBlank()) {
            throw new IllegalArgumentException("samay.header-name must not be blank");
        }
        if (jwtClaim == null || jwtClaim.isBlank()) {
            throw new IllegalArgumentException("samay.jwt-claim must not be blank");
        }
        if (strategy == ZoneStrategy.FIXED) {
            try {
                ZoneId.of(fixedZone);
            } catch (DateTimeException e) {
                throw new IllegalArgumentException(
                        "samay.fixed-zone='" + fixedZone + "' is not a valid timezone ID. "
                        + "Use a valid IANA ID (e.g. 'America/New_York') or GMT offset (e.g. 'GMT+05:30').", e);
            }
        }
    }

    // Getters and setters

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getHeaderName() { return headerName; }
    public void setHeaderName(String headerName) { this.headerName = headerName; }

    public boolean isThreadInheritable() { return threadInheritable; }
    public void setThreadInheritable(boolean threadInheritable) { this.threadInheritable = threadInheritable; }

    public ZoneStrategy getStrategy() { return strategy; }
    public void setStrategy(ZoneStrategy strategy) { this.strategy = strategy; }

    public String getJwtClaim() { return jwtClaim; }
    public void setJwtClaim(String jwtClaim) { this.jwtClaim = jwtClaim; }

    public String getFixedZone() { return fixedZone; }
    public void setFixedZone(String fixedZone) { this.fixedZone = fixedZone; }
}

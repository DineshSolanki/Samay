package io.github.dineshsolanki.samay;

/**
 * Timezone resolution strategies supported by Samay.
 */
public enum ZoneStrategy {

    /**
     * Extract timezone from an HTTP request header (default: {@code X-TimeZone}).
     */
    HEADER,

    /**
     * Extract timezone from a JWT token claim.
     */
    JWT,

    /**
     * Always use a fixed, configured timezone.
     */
    FIXED
}

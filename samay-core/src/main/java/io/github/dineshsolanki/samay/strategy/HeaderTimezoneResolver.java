package io.github.dineshsolanki.samay.strategy;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;

/**
 * Resolves timezone from an HTTP request header.
 *
 * <p>The header name is configurable (default: {@code X-TimeZone}).
 * The header value should be a valid IANA timezone ID (e.g. {@code America/New_York})
 * or a GMT offset (e.g. {@code GMT+05:30}).</p>
 */
public class HeaderTimezoneResolver implements TimezoneResolver {

    private static final Logger logger = LoggerFactory.getLogger(HeaderTimezoneResolver.class);

    private final String headerName;

    public HeaderTimezoneResolver(String headerName) {
        this.headerName = headerName;
    }

    @Override
    public ZoneId resolve(HttpServletRequest request) {
        String headerValue = request.getHeader(headerName);
        if (headerValue == null || headerValue.isBlank()) {
            logger.debug("Samay: header '{}' not found or empty", headerName);
            return null;
        }
        try {
            ZoneId zone = ZoneId.of(headerValue);
            logger.debug("Samay: resolved timezone '{}' from header '{}'", zone, headerName);
            return zone;
        } catch (Exception e) {
            logger.warn("Samay: invalid timezone '{}' in header '{}', ignoring", headerValue, headerName);
            return null;
        }
    }
}

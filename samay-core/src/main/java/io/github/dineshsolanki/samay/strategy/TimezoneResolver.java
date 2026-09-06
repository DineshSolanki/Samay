package io.github.dineshsolanki.samay.strategy;

import jakarta.servlet.http.HttpServletRequest;
import java.time.ZoneId;

/**
 * Strategy interface for resolving the client's timezone from an HTTP request.
 *
 * <p>Implementations extract timezone information from different sources
 * (headers, JWT tokens, fixed configuration, etc.).</p>
 */
public interface TimezoneResolver {

    /**
     * Resolve the client's timezone from the given request.
     *
     * @param request the current HTTP request
     * @return the resolved timezone, or {@code null} if no timezone could be determined
     *         (the caller will fall back to the system default)
     */
    ZoneId resolve(HttpServletRequest request);
}

package io.github.dineshsolanki.samay.strategy;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.util.Base64;
import java.util.Map;

/**
 * Resolves timezone from a JWT token's claim.
 *
 * <p>Extracts the JWT from the {@code Authorization: Bearer ...} header,
 * decodes the payload (without signature verification — that is the auth server's job),
 * and reads the configured claim name for a timezone identifier.</p>
 *
 * <p>Falls back to the provided fallback resolver if no JWT is present or the claim is missing/invalid.</p>
 *
 * <p><b>Requires Jackson ({@code com.fasterxml.jackson.databind.ObjectMapper}) on the classpath.</b>
 * This dependency is not pulled transitively — add it to your project if using JWT strategy.</p>
 */
public class JwtTimezoneResolver implements TimezoneResolver {

    private static final Logger logger = LoggerFactory.getLogger(JwtTimezoneResolver.class);

    private final String claimName;
    private final TimezoneResolver fallback;
    private final Object objectMapper;
    private final java.lang.reflect.Method readValueMethod;

    /**
     * @param claimName the JWT claim containing the timezone (e.g. "timezone")
     * @param fallback  resolver to use if no JWT is present; may be {@code null}
     * @throws IllegalStateException if Jackson ({@code ObjectMapper}) is not on the classpath
     */
    @SuppressWarnings("unchecked")
    public JwtTimezoneResolver(String claimName, TimezoneResolver fallback) {
        this.claimName = claimName;
        this.fallback = fallback;

        // Require Jackson at construction time — fail fast, not at runtime
        try {
            Class<?> mapperClass = Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
            this.objectMapper = mapperClass.getDeclaredConstructor().newInstance();
            this.readValueMethod = mapperClass.getMethod("readValue", String.class, Class.class);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "JwtTimezoneResolver requires Jackson (com.fasterxml.jackson.databind.ObjectMapper) "
                    + "on the classpath. Add 'jackson-databind' as a dependency.", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public ZoneId resolve(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.debug("Samay: no Bearer token found, falling back");
            return fallback != null ? fallback.resolve(request) : null;
        }

        String token = authHeader.substring(7);
        try {
            String payload = decodeJwtPayload(token);
            Map<String, Object> claims = (Map<String, Object>) readValueMethod.invoke(objectMapper, payload, Map.class);
            Object tzValue = claims.get(claimName);
            if (tzValue instanceof String tz && !tz.isBlank()) {
                ZoneId zone = ZoneId.of(tz);
                logger.debug("Samay: resolved timezone '{}' from JWT claim '{}'", zone, claimName);
                return zone;
            }
            logger.debug("Samay: JWT claim '{}' not found or empty, falling back", claimName);
        } catch (Exception e) {
            logger.warn("Samay: failed to extract timezone from JWT: {}", e.getMessage());
        }

        return fallback != null ? fallback.resolve(request) : null;
    }

    /**
     * Decode the JWT payload segment (second part) using Base64 URL decoding.
     */
    private String decodeJwtPayload(String token) {
        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid JWT: expected at least 2 parts");
        }
        return new String(Base64.getUrlDecoder().decode(parts[1]));
    }
}

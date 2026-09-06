package io.github.dineshsolanki.samay.strategy;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.ZoneId;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtTimezoneResolverTest {

    private final HeaderTimezoneResolver fallback = new HeaderTimezoneResolver("X-TimeZone");
    private final JwtTimezoneResolver resolver = new JwtTimezoneResolver("timezone", fallback);

    @Test
    void resolve_extractsTimezoneFromJwtClaim() {
        String payload = Base64.getUrlEncoder().encodeToString(
                "{\"sub\":\"123\",\"timezone\":\"Europe/Berlin\"}".getBytes());
        String token = "header." + payload + ".signature";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        assertEquals(ZoneId.of("Europe/Berlin"), resolver.resolve(request));
    }

    @Test
    void resolve_fallsBackToHeaderWhenNoJwt() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-TimeZone", "America/Chicago");

        assertEquals(ZoneId.of("America/Chicago"), resolver.resolve(request));
    }

    @Test
    void resolve_returnsNullWhenNoJwtAndNoFallback() {
        JwtTimezoneResolver noFallback = new JwtTimezoneResolver("timezone", null);
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertNull(noFallback.resolve(request));
    }

    @Test
    void resolve_fallsBackWhenClaimMissing() {
        String payload = Base64.getUrlEncoder().encodeToString(
                "{\"sub\":\"123\"}".getBytes());
        String token = "header." + payload + ".signature";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        request.addHeader("X-TimeZone", "Asia/Tokyo");

        assertEquals(ZoneId.of("Asia/Tokyo"), resolver.resolve(request));
    }

    @Test
    void resolve_fallsBackWhenTokenMalformed() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid-token");
        request.addHeader("X-TimeZone", "UTC");

        assertEquals(ZoneId.of("UTC"), resolver.resolve(request));
    }

    @Test
    void resolve_ignoresNonBearerAuth() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");
        request.addHeader("X-TimeZone", "America/Denver");

        assertEquals(ZoneId.of("America/Denver"), resolver.resolve(request));
    }
}

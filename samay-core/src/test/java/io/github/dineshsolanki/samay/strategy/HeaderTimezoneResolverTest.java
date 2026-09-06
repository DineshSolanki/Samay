package io.github.dineshsolanki.samay.strategy;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class HeaderTimezoneResolverTest {

    private final HeaderTimezoneResolver resolver = new HeaderTimezoneResolver("X-TimeZone");

    @Test
    void resolve_returnsZoneFromHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-TimeZone", "America/New_York");

        ZoneId result = resolver.resolve(request);

        assertEquals(ZoneId.of("America/New_York"), result);
    }

    @Test
    void resolve_returnsNullWhenHeaderMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertNull(resolver.resolve(request));
    }

    @Test
    void resolve_returnsNullWhenHeaderBlank() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-TimeZone", "  ");

        assertNull(resolver.resolve(request));
    }

    @Test
    void resolve_handlesGmtOffset() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-TimeZone", "GMT+05:30");

        ZoneId result = resolver.resolve(request);

        assertEquals(ZoneId.of("GMT+05:30"), result);
    }

    @Test
    void resolve_returnsNullForInvalidTimezone() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-TimeZone", "Not/A/Zone");

        assertNull(resolver.resolve(request));
    }

    @Test
    void resolve_usesCustomHeaderName() {
        HeaderTimezoneResolver custom = new HeaderTimezoneResolver("My-Custom-TZ");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("My-Custom-TZ", "Asia/Tokyo");

        assertEquals(ZoneId.of("Asia/Tokyo"), custom.resolve(request));
    }
}

package io.github.dineshsolanki.samay.strategy;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class FixedTimezoneResolverTest {

    @Test
    void resolve_alwaysReturnsFixedZone() {
        FixedTimezoneResolver resolver = new FixedTimezoneResolver(ZoneId.of("Asia/Kolkata"));
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertEquals(ZoneId.of("Asia/Kolkata"), resolver.resolve(request));
    }

    @Test
    void resolve_ignoresRequestHeaders() {
        FixedTimezoneResolver resolver = new FixedTimezoneResolver(ZoneId.of("UTC"));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-TimeZone", "America/New_York");

        assertEquals(ZoneId.of("UTC"), resolver.resolve(request));
    }
}

package io.github.dineshsolanki.samay;

import io.github.dineshsolanki.samay.strategy.HeaderTimezoneResolver;
import io.github.dineshsolanki.samay.strategy.TimezoneResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

class SamayTest {

    private Samay interceptor;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        TimezoneResolver resolver = new HeaderTimezoneResolver("X-TimeZone");
        interceptor = new Samay(resolver, false);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        Samay.clear();
    }

    @AfterEach
    void tearDown() {
        Samay.clear();
    }

    // ─── Interceptor lifecycle ──────────────────────────────────────────

    @Test
    void preHandle_setsTimezoneFromHeader() {
        request.addHeader("X-TimeZone", "America/New_York");

        interceptor.preHandle(request, response, new Object());

        assertEquals(ZoneId.of("America/New_York"), Samay.getZoneId());
    }

    @Test
    void preHandle_defaultsToSystemDefaultWhenNoHeader() {
        interceptor.preHandle(request, response, new Object());

        assertEquals(ZoneId.systemDefault(), Samay.getZoneId());
    }

    @Test
    void preHandle_defaultsToSystemDefaultForInvalidHeader() {
        request.addHeader("X-TimeZone", "Invalid/Zone");

        interceptor.preHandle(request, response, new Object());

        assertEquals(ZoneId.systemDefault(), Samay.getZoneId());
    }

    @Test
    void afterCompletion_clearsTimezone() {
        request.addHeader("X-TimeZone", "Europe/London");
        interceptor.preHandle(request, response, new Object());
        assertTrue(Samay.isSet());

        interceptor.afterCompletion(request, response, new Object(), null);

        assertFalse(Samay.isSet());
    }

    @Test
    void preHandle_alwaysReturnsTrue() {
        assertTrue(interceptor.preHandle(request, response, new Object()));
    }

    // ─── getZoneId ──────────────────────────────────────────────────────

    @Test
    void getZoneId_returnsSystemDefaultWhenNotSet() {
        assertEquals(ZoneId.systemDefault(), Samay.getZoneId());
    }

    @Test
    void getZoneId_returnsSetZone() {
        Samay.setZoneId(ZoneId.of("Asia/Tokyo"));

        assertEquals(ZoneId.of("Asia/Tokyo"), Samay.getZoneId());
    }

    @Test
    void isSet_returnsFalseWhenCleared() {
        assertFalse(Samay.isSet());
    }

    @Test
    void isSet_returnsTrueWhenSet() {
        Samay.setZoneId(ZoneId.of("UTC"));
        assertTrue(Samay.isSet());
    }

    // ─── now() ──────────────────────────────────────────────────────────

    @Test
    void now_returnsCurrentTimeInSetTimezone() {
        Samay.setZoneId(ZoneId.of("America/New_York"));

        ZonedDateTime result = Samay.now();

        assertEquals(ZoneId.of("America/New_York"), result.getZone());
        // Should be within a few seconds of now
        assertTrue(Math.abs(result.toInstant().toEpochMilli() - Instant.now().toEpochMilli()) < 5000);
    }

    @Test
    void now_usesSystemDefaultWhenNotSet() {
        ZonedDateTime result = Samay.now();

        assertEquals(ZoneId.systemDefault(), result.getZone());
    }

    // ─── atZone() ───────────────────────────────────────────────────────

    @Test
    void atZone_convertsInstantToUserTimezone() {
        Samay.setZoneId(ZoneId.of("Asia/Kolkata"));
        Instant utc = Instant.parse("2026-09-06T10:00:00Z");

        ZonedDateTime result = Samay.atZone(utc);

        assertEquals(ZoneId.of("Asia/Kolkata"), result.getZone());
        assertEquals(15, result.getHour()); // UTC+5:30
        assertEquals(30, result.getMinute());
    }

    @Test
    void atZone_returnsNullForNullInstant() {
        assertNull(Samay.atZone((Instant) null));
    }

    @Test
    void atZone_localDateTimeInterpretedAsUserTimezone() {
        Samay.setZoneId(ZoneId.of("America/Chicago"));
        LocalDateTime ldt = LocalDateTime.of(2026, 9, 6, 15, 30);

        ZonedDateTime result = Samay.atZone(ldt);

        assertEquals(ZoneId.of("America/Chicago"), result.getZone());
        assertEquals(15, result.getHour());
    }

    @Test
    void atZone_localDateStartOfDay() {
        Samay.setZoneId(ZoneId.of("Europe/Berlin"));
        LocalDate date = LocalDate.of(2026, 12, 25);

        ZonedDateTime result = Samay.atZone(date);

        assertEquals(ZoneId.of("Europe/Berlin"), result.getZone());
        assertEquals(0, result.getHour());
        assertEquals(0, result.getMinute());
    }

    // ─── format() ───────────────────────────────────────────────────────

    @Test
    void format_instantInUserTimezone() {
        Samay.setZoneId(ZoneId.of("America/New_York"));
        Instant utc = Instant.parse("2026-09-06T19:30:00Z");

        String result = Samay.format(utc, "HH:mm");

        assertEquals("15:30", result); // EDT (UTC-4)
    }

    @Test
    void format_returnsNullForNullInstant() {
        assertNull(Samay.format((Instant) null, "HH:mm"));
    }

    @Test
    void format_zonedDateTimeConvertedToUserTimezone() {
        Samay.setZoneId(ZoneId.of("Asia/Tokyo"));
        ZonedDateTime input = ZonedDateTime.of(2026, 9, 6, 10, 0, 0, 0, ZoneId.of("UTC"));

        String result = Samay.format(input, "HH:mm");

        assertEquals("19:00", result); // JST (UTC+9)
    }

    // ─── startOfDay / endOfDay ──────────────────────────────────────────

    @Test
    void startOfDay_returnsMidnightInUserTimezoneAsUtc() {
        Samay.setZoneId(ZoneId.of("America/New_York"));
        LocalDate date = LocalDate.of(2026, 9, 6);

        Instant result = Samay.startOfDay(date);

        // 2026-09-06 00:00 EDT = 2026-09-06 04:00 UTC
        assertEquals(Instant.parse("2026-09-06T04:00:00Z"), result);
    }

    @Test
    void endOfDay_returnsEndOfDayInUserTimezoneAsUtc() {
        Samay.setZoneId(ZoneId.of("America/New_York"));
        LocalDate date = LocalDate.of(2026, 9, 6);

        Instant result = Samay.endOfDay(date);

        // 2026-09-07 00:00 EDT = 2026-09-07 04:00 UTC, minus 1ns
        assertEquals(Instant.parse("2026-09-07T03:59:59.999999999Z"), result);
    }

    @Test
    void startOfDay_nullReturnsNull() {
        assertNull(Samay.startOfDay(null));
    }

    @Test
    void endOfDay_nullReturnsNull() {
        assertNull(Samay.endOfDay(null));
    }

    // ─── Initialize / inheritable ───────────────────────────────────────

    @Test
    void initialize_setsHolderOnlyOnce() {
        // First initialization (already happened in constructor via setUp)
        Samay.setZoneId(ZoneId.of("UTC"));
        assertEquals(ZoneId.of("UTC"), Samay.getZoneId());

        // Second initialization should be a no-op
        Samay.initialize(true);
        assertEquals(ZoneId.of("UTC"), Samay.getZoneId());
    }

    // ─── Legacy API ─────────────────────────────────────────────────────

    @SuppressWarnings("deprecation")
    @Test
    void getTimeZone_returnsLegacyTimezone() {
        Samay.setZoneId(ZoneId.of("Europe/Paris"));

        TimeZone tz = Samay.getTimeZone();

        assertEquals("Europe/Paris", tz.getID());
    }

    @SuppressWarnings("deprecation")
    @Test
    void getTimeZone_returnsSystemDefaultWhenNotSet() {
        TimeZone tz = Samay.getTimeZone();

        assertEquals(TimeZone.getDefault().getID(), tz.getID());
    }
}

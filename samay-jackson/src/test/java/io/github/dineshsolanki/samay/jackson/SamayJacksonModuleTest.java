package io.github.dineshsolanki.samay.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.dineshsolanki.samay.Samay;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SamayJacksonModuleTest {

    private ObjectMapper globalMapper;
    private ObjectMapper annotatedMapper;

    @BeforeEach
    void setUp() {
        // Global mode — converts all fields
        SamayJacksonProperties globalProps = new SamayJacksonProperties();
        globalProps.setGlobal(true);
        globalMapper = new ObjectMapper();
        globalMapper.registerModule(new SamayJacksonModule(globalProps));
        globalMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Annotated-only mode — only converts @SamayFormat fields
        SamayJacksonProperties annotatedProps = new SamayJacksonProperties();
        annotatedProps.setGlobal(false);
        annotatedMapper = new ObjectMapper();
        annotatedMapper.registerModule(new SamayJacksonModule(annotatedProps));
        annotatedMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Samay.setZoneId(ZoneId.of("America/New_York"));
    }

    @AfterEach
    void tearDown() {
        Samay.clear();
    }

    // ─── Global mode ────────────────────────────────────────────────────

    @Test
    void globalMode_instantConvertedToUserTimezone() throws Exception {
        Instant utc = Instant.parse("2026-09-06T19:30:00Z");

        String json = globalMapper.writeValueAsString(utc);

        assertTrue(json.contains("15:30"), "Expected 15:30 but got: " + json);
    }

    @Test
    void globalMode_zonedDateTimeConverted() throws Exception {
        ZonedDateTime utc = ZonedDateTime.of(2026, 9, 6, 19, 30, 0, 0, ZoneId.of("UTC"));

        String json = globalMapper.writeValueAsString(utc);

        assertTrue(json.contains("15:30"), "Expected 15:30 but got: " + json);
    }

    @Test
    void globalMode_localDateTimeFormatted() throws Exception {
        LocalDateTime ldt = LocalDateTime.of(2026, 9, 6, 15, 30);

        String json = globalMapper.writeValueAsString(ldt);

        assertTrue(json.contains("15:30"), "Expected 15:30 but got: " + json);
    }

    // ─── Annotated mode ─────────────────────────────────────────────────

    @Test
    void annotatedMode_annotatedFieldIsConverted() throws Exception {
        AnnotatedDto dto = new AnnotatedDto();
        dto.startTime = Instant.parse("2026-09-06T19:30:00Z");

        String json = annotatedMapper.writeValueAsString(dto);

        assertTrue(json.contains("15:30"), "Expected @SamayFormat field converted, got: " + json);
    }

    @Test
    void annotatedMode_unannotatedFieldStaysUtc() throws Exception {
        AnnotatedDto dto = new AnnotatedDto();
        dto.createdAt = Instant.parse("2026-09-06T19:30:00Z");

        String json = annotatedMapper.writeValueAsString(dto);

        // Should stay UTC (2026-09-06T19:30:00Z → 19:30 UTC, not 15:30 EDT)
        assertTrue(json.contains("19:30"), "Expected unannotated field to stay UTC, got: " + json);
    }

    // ─── Custom pattern ─────────────────────────────────────────────────

    @Test
    void customPattern_isApplied() throws Exception {
        SamayJacksonProperties props = new SamayJacksonProperties();
        props.setGlobal(true);
        props.setDateTimePattern("dd/MM/yyyy HH:mm");
        ObjectMapper customMapper = new ObjectMapper();
        customMapper.registerModule(new SamayJacksonModule(props));

        Instant utc = Instant.parse("2026-09-06T19:30:00Z");
        String json = customMapper.writeValueAsString(utc);

        assertTrue(json.contains("06/09/2026 15:30"), "Expected 06/09/2026 15:30 but got: " + json);
    }

    // ─── Timezone-dependent output ──────────────────────────────────────

    @Test
    void differentTimezone_differentOutput() throws Exception {
        Instant utc = Instant.parse("2026-09-06T12:00:00Z");

        Samay.setZoneId(ZoneId.of("Asia/Tokyo"));
        String tokyo = globalMapper.writeValueAsString(utc);

        Samay.setZoneId(ZoneId.of("America/Los_Angeles"));
        String la = globalMapper.writeValueAsString(utc);

        assertTrue(tokyo.contains("21:00"), "Tokyo expected 21:00 but got: " + tokyo);
        assertTrue(la.contains("05:00"), "LA expected 05:00 but got: " + la);
    }

    // ─── Null handling ──────────────────────────────────────────────────

    @Test
    void nullInstant_returnsNull() throws Exception {
        String json = globalMapper.writeValueAsString(null);
        assertEquals("null", json);
    }

    // ─── ISO-8601 fallback ──────────────────────────────────────────────

    @Test
    void deserializer_iso8601InstantParsedAsFallback() throws Exception {
        // Input uses ISO-8601 with Z suffix, but default pattern expects XXX offset
        String json = "\"2026-09-06T19:30:00Z\"";

        Instant result = globalMapper.readValue(json, Instant.class);

        assertEquals(Instant.parse("2026-09-06T19:30:00Z"), result);
    }

    @Test
    void deserializer_iso8601ZonedDateTimeParsedAsFallback() throws Exception {
        String json = "\"2026-09-06T19:30:00+00:00\"";

        ZonedDateTime result = globalMapper.readValue(json, ZonedDateTime.class);

        assertEquals(Instant.parse("2026-09-06T19:30:00Z"), result.toInstant());
    }

    @Test
    void deserializer_iso8601LocalDateTimeParsedAsFallback() throws Exception {
        String json = "\"2026-09-06T15:30:00\"";

        LocalDateTime result = globalMapper.readValue(json, LocalDateTime.class);

        assertEquals(LocalDateTime.of(2026, 9, 6, 15, 30, 0), result);
    }

    // ─── Test DTO ───────────────────────────────────────────────────────

    static class AnnotatedDto {
        @SamayFormat
        public Instant startTime;

        public Instant createdAt; // not annotated — should stay UTC
    }
}

package io.github.dineshsolanki.samay.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.github.dineshsolanki.samay.Samay;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson module that serializes/deserializes date/time types in the user's timezone.
 *
 * <p>By default, only fields annotated with {@link SamayFormat} are converted.
 * Set {@code samay.jackson.global=true} to convert all date/time fields.</p>
 */
public class SamayJacksonModule extends SimpleModule {

    private static final long serialVersionUID = 1L;

    public SamayJacksonModule(SamayJacksonProperties properties) {
        super("SamayDateTimeModule");

        String dateTimePattern = properties.getDateTimePattern();
        // LocalDateTime has no offset — strip offset tokens from pattern
        String localDateTimePattern = stripOffsetPattern(dateTimePattern);
        String datePattern = properties.getDatePattern();
        boolean global = properties.isGlobal();

        // Serializers — wrap with contextual awareness
        addSerializer(Instant.class, new SamayInstantSerializer(dateTimePattern, global));
        addSerializer(ZonedDateTime.class, new SamayZonedDateTimeSerializer(dateTimePattern, global));
        addSerializer(LocalDateTime.class, new SamayLocalDateTimeSerializer(localDateTimePattern, global));
        addSerializer(LocalDate.class, new SamayLocalDateSerializer(datePattern, global));

        // Deserializers — always parse the configured pattern
        addDeserializer(Instant.class, new SamayInstantDeserializer(dateTimePattern));
        addDeserializer(ZonedDateTime.class, new SamayZonedDateTimeDeserializer(dateTimePattern));
        addDeserializer(LocalDateTime.class, new SamayLocalDateTimeDeserializer(localDateTimePattern));
        addDeserializer(LocalDate.class, new SamayLocalDateDeserializer(datePattern));
    }

    private static String stripOffsetPattern(String pattern) {
        return pattern.replace("XXX", "").replace("XX", "").replace("X", "")
                .replace("VV", "").replaceAll(":+$", "").trim();
    }

    // ─── Annotation-aware serializers ───────────────────────────────────

    /**
     * Base class for Samay serializers. Implements {@link ContextualSerializer} to
     * check for {@link SamayFormat} annotation on the specific field being serialized.
     */
    private static abstract class SamaySerializer<T> extends StdSerializer<T> implements ContextualSerializer {
        private final String pattern;
        private final boolean global;
        private boolean active; // whether this instance should convert timezone

        protected SamaySerializer(Class<T> handledType, String pattern, boolean global) {
            super(handledType);
            this.pattern = pattern;
            this.global = global;
            this.active = global; // default: active only if global mode
        }

        protected SamaySerializer(Class<T> handledType, String pattern, boolean global, boolean active) {
            super(handledType);
            this.pattern = pattern;
            this.global = global;
            this.active = active;
        }

        @Override
        @SuppressWarnings("unchecked")
        public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
            if (property == null) {
                return this;
            }
            boolean fieldAnnotated = property.getAnnotation(SamayFormat.class) != null;
            boolean shouldBeActive = global || fieldAnnotated;
            return createInstance(pattern, global, shouldBeActive);
        }

        protected abstract JsonSerializer<T> createInstance(String pattern, boolean global, boolean active);

        protected String getPattern() { return pattern; }
        protected boolean isActive() { return active; }
    }

    private static class SamayInstantSerializer extends SamaySerializer<Instant> {
        SamayInstantSerializer(String pattern, boolean global) { super(Instant.class, pattern, global); }
        private SamayInstantSerializer(String pattern, boolean global, boolean active) {
            super(Instant.class, pattern, global, active);
        }

        @Override
        protected JsonSerializer<Instant> createInstance(String pattern, boolean global, boolean active) {
            return new SamayInstantSerializer(pattern, global, active);
        }

        @Override
        public void serialize(Instant value, JsonGenerator gen, SerializerProvider prov) throws IOException {
            if (value == null) { gen.writeNull(); return; }
            if (isActive()) {
                gen.writeString(Samay.atZone(value).format(DateTimeFormatter.ofPattern(getPattern())));
            } else {
                gen.writeString(value.atZone(ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern(getPattern())));
            }
        }
    }

    private static class SamayZonedDateTimeSerializer extends SamaySerializer<ZonedDateTime> {
        SamayZonedDateTimeSerializer(String pattern, boolean global) { super(ZonedDateTime.class, pattern, global); }
        private SamayZonedDateTimeSerializer(String pattern, boolean global, boolean active) {
            super(ZonedDateTime.class, pattern, global, active);
        }

        @Override
        protected JsonSerializer<ZonedDateTime> createInstance(String pattern, boolean global, boolean active) {
            return new SamayZonedDateTimeSerializer(pattern, global, active);
        }

        @Override
        public void serialize(ZonedDateTime value, JsonGenerator gen, SerializerProvider prov) throws IOException {
            if (value == null) { gen.writeNull(); return; }
            if (isActive()) {
                gen.writeString(value.withZoneSameInstant(Samay.getZoneId())
                        .format(DateTimeFormatter.ofPattern(getPattern())));
            } else {
                gen.writeString(value.format(DateTimeFormatter.ofPattern(getPattern())));
            }
        }
    }

    private static class SamayLocalDateTimeSerializer extends SamaySerializer<LocalDateTime> {
        SamayLocalDateTimeSerializer(String pattern, boolean global) { super(LocalDateTime.class, pattern, global); }
        private SamayLocalDateTimeSerializer(String pattern, boolean global, boolean active) {
            super(LocalDateTime.class, pattern, global, active);
        }

        @Override
        protected JsonSerializer<LocalDateTime> createInstance(String pattern, boolean global, boolean active) {
            return new SamayLocalDateTimeSerializer(pattern, global, active);
        }

        @Override
        public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider prov) throws IOException {
            if (value == null) { gen.writeNull(); return; }
            gen.writeString(value.format(DateTimeFormatter.ofPattern(getPattern())));
        }
    }

    private static class SamayLocalDateSerializer extends SamaySerializer<LocalDate> {
        SamayLocalDateSerializer(String pattern, boolean global) { super(LocalDate.class, pattern, global); }
        private SamayLocalDateSerializer(String pattern, boolean global, boolean active) {
            super(LocalDate.class, pattern, global, active);
        }

        @Override
        protected JsonSerializer<LocalDate> createInstance(String pattern, boolean global, boolean active) {
            return new SamayLocalDateSerializer(pattern, global, active);
        }

        @Override
        public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider prov) throws IOException {
            if (value == null) { gen.writeNull(); return; }
            gen.writeString(value.format(DateTimeFormatter.ofPattern(getPattern())));
        }
    }

    // ─── Deserializers (try configured pattern, fall back to ISO-8601) ──

    private static final DateTimeFormatter ISO_INSTANT = DateTimeFormatter.ISO_INSTANT;
    private static final DateTimeFormatter ISO_ZONED = DateTimeFormatter.ISO_ZONED_DATE_TIME;
    private static final DateTimeFormatter ISO_LOCAL = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    private static class SamayInstantDeserializer extends StdDeserializer<Instant> {
        private final String pattern;
        private final DateTimeFormatter formatter;
        SamayInstantDeserializer(String pattern) { super(Instant.class); this.pattern = pattern; this.formatter = DateTimeFormatter.ofPattern(pattern); }

        @Override
        public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String text = p.getText();
            if (text == null || text.isBlank()) return null;
            // Try configured pattern first, then ISO-8601
            try {
                return ZonedDateTime.parse(text, formatter)
                        .withZoneSameInstant(Samay.getZoneId())
                        .toInstant();
            } catch (Exception e) {
                return Instant.parse(text); // ISO-8601 fallback (handles Z, +00:00, etc.)
            }
        }
    }

    private static class SamayZonedDateTimeDeserializer extends StdDeserializer<ZonedDateTime> {
        private final String pattern;
        private final DateTimeFormatter formatter;
        SamayZonedDateTimeDeserializer(String pattern) { super(ZonedDateTime.class); this.pattern = pattern; this.formatter = DateTimeFormatter.ofPattern(pattern); }

        @Override
        public ZonedDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String text = p.getText();
            if (text == null || text.isBlank()) return null;
            try {
                return ZonedDateTime.parse(text, formatter)
                        .withZoneSameInstant(Samay.getZoneId());
            } catch (Exception e) {
                return ZonedDateTime.parse(text, ISO_ZONED) // ISO-8601 fallback
                        .withZoneSameInstant(Samay.getZoneId());
            }
        }
    }

    private static class SamayLocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {
        private final String pattern;
        private final DateTimeFormatter formatter;
        SamayLocalDateTimeDeserializer(String pattern) { super(LocalDateTime.class); this.pattern = pattern; this.formatter = DateTimeFormatter.ofPattern(pattern); }

        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String text = p.getText();
            if (text == null || text.isBlank()) return null;
            try {
                return LocalDateTime.parse(text, formatter);
            } catch (Exception e) {
                return LocalDateTime.parse(text, ISO_LOCAL); // ISO-8601 fallback
            }
        }
    }

    private static class SamayLocalDateDeserializer extends StdDeserializer<LocalDate> {
        private final String pattern;
        private final DateTimeFormatter formatter;
        SamayLocalDateDeserializer(String pattern) { super(LocalDate.class); this.pattern = pattern; this.formatter = DateTimeFormatter.ofPattern(pattern); }

        @Override
        public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String text = p.getText();
            if (text == null || text.isBlank()) return null;
            try {
                return LocalDate.parse(text, formatter);
            } catch (Exception e) {
                return LocalDate.parse(text, ISO_DATE); // ISO-8601 fallback
            }
        }
    }
}

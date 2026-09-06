package io.github.dineshsolanki.samay.jackson;

/**
 * Configuration properties for Samay's Jackson integration.
 *
 * <pre>{@code
 * samay:
 *   jackson:
 *     auto-convert: true
 *     global: false
 *     date-time-pattern: "yyyy-MM-dd'T'HH:mm:ssXXX"
 * }</pre>
 */
public class SamayJacksonProperties {

    /**
     * Whether to enable timezone-aware date/time serialization.
     * When true, fields annotated with {@link SamayFormat} are converted.
     */
    private boolean autoConvert = false;

    /**
     * When true, ALL date/time fields are converted (not just annotated ones).
     * Set this when you want blanket conversion without annotating every field.
     */
    private boolean global = false;

    /**
     * The date-time format pattern for serializing Instant/ZonedDateTime fields.
     * Uses {@link java.time.format.DateTimeFormatter} syntax.
     */
    private String dateTimePattern = "yyyy-MM-dd'T'HH:mm:ssXXX";

    /**
     * The date-only format pattern for serializing LocalDate fields.
     */
    private String datePattern = "yyyy-MM-dd";

    public boolean isAutoConvert() { return autoConvert; }
    public void setAutoConvert(boolean autoConvert) { this.autoConvert = autoConvert; }

    public boolean isGlobal() { return global; }
    public void setGlobal(boolean global) { this.global = global; }

    public String getDateTimePattern() { return dateTimePattern; }
    public void setDateTimePattern(String dateTimePattern) { this.dateTimePattern = dateTimePattern; }

    public String getDatePattern() { return datePattern; }
    public void setDatePattern(String datePattern) { this.datePattern = datePattern; }
}

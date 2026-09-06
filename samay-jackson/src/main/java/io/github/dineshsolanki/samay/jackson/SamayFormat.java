package io.github.dineshsolanki.samay.jackson;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a date/time field for automatic timezone conversion in JSON serialization.
 *
 * <p>When {@code samay.jackson.auto-convert=true}, only fields annotated with
 * {@code @SamayFormat} will have their timezone converted to the client's timezone.
 * All other date/time fields serialize as-is (typically UTC).</p>
 *
 * <pre>{@code
 * public class EventDto {
 *     @SamayFormat
 *     private Instant startTime;  // converted to user's timezone
 *
 *     private Instant createdAt;  // stays UTC
 * }
 * }</pre>
 *
 * <p>When {@code samay.jackson.global=true}, all date/time fields are converted
 * regardless of this annotation (legacy behavior).</p>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SamayFormat {
}

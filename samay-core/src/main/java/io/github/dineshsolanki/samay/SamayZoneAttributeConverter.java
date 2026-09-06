package io.github.dineshsolanki.samay;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.sql.Timestamp;
import java.time.Instant;

/**
 * JPA {@link AttributeConverter} that maps {@link Instant} to {@link Timestamp} for database storage.
 *
 * <p>Stores all timestamps as UTC. To display in the user's timezone, use
 * {@link Samay#atZone(Instant)} on read.</p>
 *
 * <p>Apply to individual fields:</p>
 * <pre>{@code
 * @Column(name = "created_at")
 * @Convert(converter = SamayZoneAttributeConverter.class)
 * private Instant createdAt;
 * }</pre>
 *
 * <p>Or make it apply automatically by extending with {@code @Converter(autoApply = true)}
 * in your own project.</p>
 */
@Converter
public class SamayZoneAttributeConverter implements AttributeConverter<Instant, Timestamp> {

    @Override
    public Timestamp convertToDatabaseColumn(Instant attribute) {
        return attribute != null ? Timestamp.from(attribute) : null;
    }

    @Override
    public Instant convertToEntityAttribute(Timestamp dbData) {
        return dbData != null ? dbData.toInstant() : null;
    }
}

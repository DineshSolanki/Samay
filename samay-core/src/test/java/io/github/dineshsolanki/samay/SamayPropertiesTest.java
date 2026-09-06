package io.github.dineshsolanki.samay;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SamayPropertiesTest {

    @Test
    void validate_defaultProperties_passes() {
        SamayProperties props = new SamayProperties();
        assertDoesNotThrow(props::validate);
    }

    @Test
    void validate_fixedWithValidZone_passes() {
        SamayProperties props = new SamayProperties();
        props.setStrategy(ZoneStrategy.FIXED);
        props.setFixedZone("Asia/Kolkata");
        assertDoesNotThrow(props::validate);
    }

    @Test
    void validate_fixedWithInvalidZone_throws() {
        SamayProperties props = new SamayProperties();
        props.setStrategy(ZoneStrategy.FIXED);
        props.setFixedZone("Not/A/Zone");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, props::validate);
        assertTrue(ex.getMessage().contains("samay.fixed-zone"));
        assertTrue(ex.getMessage().contains("Not/A/Zone"));
    }

    @Test
    void validate_blankHeaderName_throws() {
        SamayProperties props = new SamayProperties();
        props.setHeaderName("  ");

        assertThrows(IllegalArgumentException.class, props::validate);
    }

    @Test
    void validate_nullHeaderName_throws() {
        SamayProperties props = new SamayProperties();
        props.setHeaderName(null);

        assertThrows(IllegalArgumentException.class, props::validate);
    }

    @Test
    void validate_blankJwtClaim_throws() {
        SamayProperties props = new SamayProperties();
        props.setJwtClaim("  ");

        assertThrows(IllegalArgumentException.class, props::validate);
    }

    @Test
    void validate_headerStrategy_ignoresFixedZone() {
        SamayProperties props = new SamayProperties();
        props.setStrategy(ZoneStrategy.HEADER);
        props.setFixedZone("invalid"); // doesn't matter for HEADER strategy
        assertDoesNotThrow(props::validate);
    }
}

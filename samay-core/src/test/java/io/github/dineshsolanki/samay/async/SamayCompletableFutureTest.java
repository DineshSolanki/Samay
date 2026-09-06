package io.github.dineshsolanki.samay.async;

import io.github.dineshsolanki.samay.Samay;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class SamayCompletableFutureTest {

    @AfterEach
    void tearDown() {
        Samay.clear();
    }

    @Test
    void supplyAsync_propagatesTimezone() throws Exception {
        Samay.setZoneId(ZoneId.of("Asia/Kolkata"));

        CompletableFuture<ZoneId> future = SamayCompletableFuture.supplyAsync(Samay::getZoneId);

        assertEquals(ZoneId.of("Asia/Kolkata"), future.get());
    }

    @Test
    void runAsync_propagatesTimezone() throws Exception {
        Samay.setZoneId(ZoneId.of("America/New_York"));
        AtomicReference<ZoneId> captured = new AtomicReference<>();

        CompletableFuture<Void> future = SamayCompletableFuture.runAsync(() -> {
            captured.set(Samay.getZoneId());
        });

        future.get();
        assertEquals(ZoneId.of("America/New_York"), captured.get());
    }

    @Test
    void supplyAsync_cleansUpAfterCompletion() throws Exception {
        Samay.setZoneId(ZoneId.of("Europe/Paris"));

        CompletableFuture<ZoneId> future = SamayCompletableFuture.supplyAsync(() -> {
            ZoneId zone = Samay.getZoneId();
            return zone;
        });

        future.get();
        // Main thread should still have its zone
        assertEquals(ZoneId.of("Europe/Paris"), Samay.getZoneId());
    }

    @Test
    void supplyAsync_handlesNoZoneSet() throws Exception {
        // No zone set — should not throw
        CompletableFuture<Boolean> future = SamayCompletableFuture.supplyAsync(Samay::isSet);

        assertFalse(future.get());
    }
}

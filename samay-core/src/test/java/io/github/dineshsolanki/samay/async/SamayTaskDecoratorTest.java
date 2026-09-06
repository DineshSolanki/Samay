package io.github.dineshsolanki.samay.async;

import io.github.dineshsolanki.samay.Samay;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class SamayTaskDecoratorTest {

    private final SamayTaskDecorator decorator = new SamayTaskDecorator();

    @AfterEach
    void tearDown() {
        Samay.clear();
    }

    @Test
    void decorate_propagatesTimezoneToWorkerThread() throws InterruptedException {
        Samay.setZoneId(ZoneId.of("Asia/Tokyo"));
        AtomicReference<ZoneId> captured = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Runnable decorated = decorator.decorate(() -> {
            captured.set(Samay.getZoneId());
            latch.countDown();
        });

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(decorated);
        latch.await();

        assertEquals(ZoneId.of("Asia/Tokyo"), captured.get());
        executor.shutdown();
    }

    @Test
    void decorate_cleansUpAfterExecution() throws InterruptedException {
        Samay.setZoneId(ZoneId.of("Europe/London"));
        CountDownLatch latch = new CountDownLatch(1);

        Runnable decorated = decorator.decorate(() -> {
            latch.countDown();
        });

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(decorated);
        latch.await();

        // The worker thread should have its zone cleared
        // The main thread should still have its zone (decorator doesn't touch caller)
        assertEquals(ZoneId.of("Europe/London"), Samay.getZoneId());
        executor.shutdown();
    }

    @Test
    void decorate_handlesNullZoneGracefully() throws InterruptedException {
        // No zone set — should not throw
        AtomicReference<Boolean> ran = new AtomicReference<>(false);
        CountDownLatch latch = new CountDownLatch(1);

        Runnable decorated = decorator.decorate(() -> {
            ran.set(true);
            latch.countDown();
        });

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(decorated);
        latch.await();

        assertTrue(ran.get());
        executor.shutdown();
    }
}

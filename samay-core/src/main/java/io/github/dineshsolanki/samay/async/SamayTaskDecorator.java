package io.github.dineshsolanki.samay.async;

import io.github.dineshsolanki.samay.Samay;
import org.springframework.core.task.TaskDecorator;

import java.time.ZoneId;

/**
 * Spring {@link TaskDecorator} that propagates the Samay timezone context
 * from the calling thread to the worker thread.
 *
 * <p>Use this with {@code @Async} executors and {@code ThreadPoolTaskExecutor}:</p>
 * <pre>{@code
 * @Bean
 * public TaskDecorator samayTaskDecorator() {
 *     return new SamayTaskDecorator();
 * }
 *
 * @Bean
 * public Executor taskExecutor() {
 *     ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
 *     executor.setTaskDecorator(samayTaskDecorator());
 *     executor.initialize();
 *     return executor;
 * }
 * }</pre>
 */
public class SamayTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        ZoneId zone = Samay.isSet() ? Samay.getZoneId() : null;
        return () -> {
            try {
                if (zone != null) {
                    Samay.setZoneId(zone);
                }
                runnable.run();
            } finally {
                Samay.clear();
            }
        };
    }
}

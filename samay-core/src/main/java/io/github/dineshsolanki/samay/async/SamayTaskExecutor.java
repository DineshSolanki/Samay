package io.github.dineshsolanki.samay.async;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * A convenience {@link ThreadPoolTaskExecutor} pre-configured with {@link SamayTaskDecorator}
 * for automatic timezone propagation to async threads.
 *
 * <pre>{@code
 * @Bean
 * public SamayTaskExecutor taskExecutor() {
 *     SamayTaskExecutor executor = new SamayTaskExecutor();
 *     executor.setCorePoolSize(4);
 *     executor.setMaxPoolSize(8);
 *     return executor;
 * }
 * }</pre>
 */
public class SamayTaskExecutor extends ThreadPoolTaskExecutor {

    public SamayTaskExecutor() {
        setTaskDecorator(new SamayTaskDecorator());
    }
}

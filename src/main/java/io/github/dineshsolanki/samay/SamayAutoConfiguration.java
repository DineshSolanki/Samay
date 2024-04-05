package io.github.dineshsolanki.samay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration
@ConditionalOnClass(WebMvcConfigurer.class)
public class SamayAutoConfiguration {
    private final static Logger logger = LoggerFactory.getLogger(Samay.class);

    @Value("${samay.thread.inheritable}")
    private boolean isInheritable;

    @Bean
    public Samay timeZoneInterceptor() {
        return new Samay(isInheritable);
    }

    public class TimeZoneInterceptorConfig implements WebMvcConfigurer {
        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            logger.info("Samay is enabled");
            registry.addInterceptor(timeZoneInterceptor());
        }
    }
}

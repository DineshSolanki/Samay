package io.github.dineshsolanki.samay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnClass(WebMvcConfigurer.class)
public class SamayAutoConfiguration {
    private final static Logger logger = LoggerFactory.getLogger(Samay.class);
    @Bean
    public Samay timeZoneInterceptor() {
        return new Samay();
    }

    @Configuration
    public class TimeZoneInterceptorConfig implements WebMvcConfigurer {
        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            logger.info("Samay is enabled");
            registry.addInterceptor(timeZoneInterceptor());
        }
    }
}

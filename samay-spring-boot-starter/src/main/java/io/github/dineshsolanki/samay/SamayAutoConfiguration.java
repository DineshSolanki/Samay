package io.github.dineshsolanki.samay;

import io.github.dineshsolanki.samay.async.SamayTaskDecorator;
import io.github.dineshsolanki.samay.strategy.FixedTimezoneResolver;
import io.github.dineshsolanki.samay.strategy.HeaderTimezoneResolver;
import io.github.dineshsolanki.samay.strategy.JwtTimezoneResolver;
import io.github.dineshsolanki.samay.strategy.TimezoneResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;

/**
 * Auto-configuration for Samay. Registers the timezone resolver, task decorator,
 * and framework-specific integrations (WebMVC or WebFlux).
 *
 * <p>The base configuration (resolver, properties, task decorator) loads unconditionally.
 * WebMVC and WebFlux integrations are in separate inner configurations that activate
 * based on classpath detection.</p>
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "samay", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SamayProperties.class)
public class SamayAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(SamayAutoConfiguration.class);

    // ─── Base beans (always loaded) ─────────────────────────────────────

    @Bean
    @ConditionalOnMissingBean(TimezoneResolver.class)
    public TimezoneResolver samayTimezoneResolver(SamayProperties properties) {
        TimezoneResolver resolver = switch (properties.getStrategy()) {
            case HEADER -> new HeaderTimezoneResolver(properties.getHeaderName());
            case JWT -> {
                TimezoneResolver fallback = new HeaderTimezoneResolver(properties.getHeaderName());
                yield new JwtTimezoneResolver(properties.getJwtClaim(), fallback);
            }
            case FIXED -> new FixedTimezoneResolver(ZoneId.of(properties.getFixedZone()));
        };
        logger.info("Samay: using {} timezone resolver strategy", properties.getStrategy());
        return resolver;
    }

    @Bean
    @ConditionalOnMissingBean(SamayTaskDecorator.class)
    public SamayTaskDecorator samayTaskDecorator() {
        return new SamayTaskDecorator();
    }

    // ─── WebMVC integration ─────────────────────────────────────────────

    @Configuration
    @ConditionalOnClass(name = "org.springframework.web.servlet.config.annotation.WebMvcConfigurer")
    static class SamayWebMvcConfiguration {

        @Bean
        @ConditionalOnMissingBean(Samay.class)
        public Samay samayInterceptor(TimezoneResolver resolver, SamayProperties properties) {
            return new Samay(resolver, properties.isThreadInheritable());
        }

        @Bean
        public SamayWebMvcConfigurer samayWebMvcConfigurer(Samay interceptor) {
            return new SamayWebMvcConfigurer(interceptor);
        }
    }

    // ─── WebFlux integration ────────────────────────────────────────────

    @Configuration
    @ConditionalOnClass(name = {"org.springframework.web.server.WebFilter", "reactor.core.publisher.Mono"})
    static class SamayWebFluxConfiguration {

        @Bean
        @ConditionalOnMissingBean(SamayWebFilter.class)
        public SamayWebFilter samayWebFilter(SamayProperties properties) {
            return new SamayWebFilter(properties);
        }
    }
}

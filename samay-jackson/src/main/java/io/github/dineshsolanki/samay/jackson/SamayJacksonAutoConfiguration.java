package io.github.dineshsolanki.samay.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration that registers {@link SamayJacksonModule} when Jackson is on the classpath
 * and {@code samay.jackson.auto-convert=true}.
 */
@AutoConfiguration
@ConditionalOnClass(ObjectMapper.class)
@ConditionalOnProperty(prefix = "samay.jackson", name = "auto-convert", havingValue = "true")
@EnableConfigurationProperties(SamayJacksonProperties.class)
public class SamayJacksonAutoConfiguration {

    @Bean
    public SamayJacksonModule samayJacksonModule(SamayJacksonProperties properties) {
        return new SamayJacksonModule(properties);
    }
}

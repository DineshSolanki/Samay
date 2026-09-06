package io.github.dineshsolanki.samay;

import io.github.dineshsolanki.samay.async.SamayTaskDecorator;
import io.github.dineshsolanki.samay.strategy.FixedTimezoneResolver;
import io.github.dineshsolanki.samay.strategy.HeaderTimezoneResolver;
import io.github.dineshsolanki.samay.strategy.JwtTimezoneResolver;
import io.github.dineshsolanki.samay.strategy.TimezoneResolver;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.assertj.core.api.Assertions.assertThat;

class SamayAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SamayAutoConfiguration.class));

    @Test
    void defaultConfiguration_registersHeaderResolver() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(Samay.class);
            assertThat(context).hasSingleBean(TimezoneResolver.class);
            assertThat(context).hasSingleBean(SamayTaskDecorator.class);
            assertThat(context).hasSingleBean(WebMvcConfigurer.class);
            assertThat(context.getBean(TimezoneResolver.class)).isInstanceOf(HeaderTimezoneResolver.class);
        });
    }

    @Test
    void strategyJwt_registersJwtResolver() {
        contextRunner
                .withPropertyValues("samay.strategy=jwt", "samay.jwt-claim=zone")
                .run(context -> {
                    assertThat(context.getBean(TimezoneResolver.class)).isInstanceOf(JwtTimezoneResolver.class);
                });
    }

    @Test
    void strategyFixed_registersFixedResolver() {
        contextRunner
                .withPropertyValues("samay.strategy=fixed", "samay.fixed-zone=Asia/Kolkata")
                .run(context -> {
                    assertThat(context.getBean(TimezoneResolver.class)).isInstanceOf(FixedTimezoneResolver.class);
                });
    }

    @Test
    void disabled_doesNotRegisterBeans() {
        contextRunner
                .withPropertyValues("samay.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(Samay.class);
                    assertThat(context).doesNotHaveBean(TimezoneResolver.class);
                });
    }

    @Test
    void customHeaderName_isUsed() {
        contextRunner
                .withPropertyValues("samay.header-name=My-Timezone")
                .run(context -> {
                    assertThat(context).hasSingleBean(HeaderTimezoneResolver.class);
                });
    }
}

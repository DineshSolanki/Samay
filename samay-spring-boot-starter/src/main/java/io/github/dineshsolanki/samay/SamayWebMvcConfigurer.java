package io.github.dineshsolanki.samay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers the Samay timezone interceptor for all request paths.
 */
public class SamayWebMvcConfigurer implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(SamayWebMvcConfigurer.class);

    private final Samay interceptor;

    public SamayWebMvcConfigurer(Samay interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        logger.info("Samay: registering timezone interceptor");
        registry.addInterceptor(interceptor).addPathPatterns("/**");
    }
}

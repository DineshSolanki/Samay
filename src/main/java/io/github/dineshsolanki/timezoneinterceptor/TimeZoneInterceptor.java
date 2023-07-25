package io.github.dineshsolanki.timezoneinterceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.TimeZone;

public class TimeZoneInterceptor  implements HandlerInterceptor {
    private static final ThreadLocal<TimeZone> TIME_ZONE_THREAD_LOCAL = new ThreadLocal<>();
    private final static Logger logger = LoggerFactory.getLogger(TimeZoneInterceptor.class);
    @Value("${time-zone-interceptor.header-name:X-TimeZone}")
    private String timeZoneHeaderName;

    //Tests won't work without explicitly setting the header name
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        logger.info("TimeZoneInterceptor:: Inside preHandle");
        String timeZoneHeader = request.getHeader(timeZoneHeaderName);
        if (timeZoneHeader != null) {
            logger.info("TimeZoneInterceptor:: found header {}", timeZoneHeader);
            TimeZone timeZone = TimeZone.getTimeZone(timeZoneHeader);
            TIME_ZONE_THREAD_LOCAL.set(timeZone);
        } else {
            logger.info("TimeZoneInterceptor:: header not found, setting default time zone");
            TIME_ZONE_THREAD_LOCAL.set(TimeZone.getDefault());
        }
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        logger.info("TimeZoneInterceptor:: Inside afterCompletion");
        TIME_ZONE_THREAD_LOCAL.remove();
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
    
    public static TimeZone getTimeZone() {
        return TIME_ZONE_THREAD_LOCAL.get();
    }
}

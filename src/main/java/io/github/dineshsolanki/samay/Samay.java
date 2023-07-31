package io.github.dineshsolanki.samay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.TimeZone;

/**
 * A class that intercepts requests and sets the time zone for the current thread.
 */
public class Samay implements HandlerInterceptor {
    private static final ThreadLocal<TimeZone> TIME_ZONE_THREAD_LOCAL = new ThreadLocal<>();
    private final static Logger logger = LoggerFactory.getLogger(Samay.class);
    @Value("${samay.header-name:X-TimeZone}")
    private String timeZoneHeaderName;

    /**
     * Sets the time zone for the current thread based on the value of the "timeZoneHeaderName" header in the HttpServletRequest object.
     *
     * @param request The HttpServletRequest object representing the current request.
     * @param response The HttpServletResponse object representing the current response.
     * @param handler The Object representing the handler that is being executed for the current request.
     * @return true if the request should proceed with further processing, false if it should be terminated.
     * @throws Exception if an error occurs while processing the request.
     */ //Tests won't work without explicitly setting the header name
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        logger.info("Samay:: Inside preHandle");
        String timeZoneHeader = request.getHeader(timeZoneHeaderName);
        if (timeZoneHeader != null) {
            logger.info("Samay:: found header {}", timeZoneHeader);
            TimeZone timeZone = TimeZone.getTimeZone(timeZoneHeader);
            TIME_ZONE_THREAD_LOCAL.set(timeZone);
        } else {
            logger.info("Samay:: header not found, setting default time zone");
            TIME_ZONE_THREAD_LOCAL.set(TimeZone.getDefault());
        }
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    /**
     * Performs cleanup after the request has been completed.
     * Removes the time zone from the current thread.
     *
     * @param request The HttpServletRequest object representing the completed request.
     * @param response The HttpServletResponse object representing the completed response.
     * @param handler The Object representing the handler that was executed for the request.
     * @param ex The Exception that occurred during the request processing, or null if no exception occurred.
     * @throws Exception if an error occurs while performing cleanup.
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        logger.info("Samay:: Inside afterCompletion");
        TIME_ZONE_THREAD_LOCAL.remove();
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
    
    /**
     * Returns the time zone associated with the current thread.
     *
     * @return the time zone associated with the current thread
     */
    public static TimeZone getTimeZone() {
        return TIME_ZONE_THREAD_LOCAL.get();
    }
}

package io.github.dineshsolanki.timezoneinterceptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.HandlerExecutionChain;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class TimeZoneInterceptorTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HandlerExecutionChain handler;

    @InjectMocks
    private TimeZoneInterceptor interceptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

//    @Test
//    void testTimeZoneInterceptor_DefaultHeader() throws Exception {
//        when(request.getHeader("X-TimeZone")).thenReturn("America/New_York");
//
//        boolean result = interceptor.preHandle(request, response, handler);
//
//        assertTrue(result);
//        assertEquals("America/New_York", TimeZoneInterceptor.getTimeZone().getID());
//    }
//    
//    @Test
//    void testTimeZoneInterceptor_NoHeader() throws Exception {
//        boolean result = interceptor.preHandle(request, response, handler);
//
//        assertTrue(result);
//        assertEquals("GMT", TimeZoneInterceptor.getTimeZone().getID()); // Default to GMT if header not present
//    }

    // Add more tests as needed to cover different scenarios and edge cases
}

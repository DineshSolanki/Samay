package io.github.dineshsolanki.samay;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.HandlerExecutionChain;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SamayTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HandlerExecutionChain handler;

    @InjectMocks
    private Samay interceptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

//    @Test
//    void testSamay_DefaultHeader() throws Exception {
//        when(request.getHeader("X-TimeZone")).thenReturn("America/New_York");
//
//        boolean result = interceptor.preHandle(request, response, handler);
//
//        assertTrue(result);
//        assertEquals("America/New_York", Samay.getTimeZone().getID());
//    }
//    
//    @Test
//    void testSamay_NoHeader() throws Exception {
//        boolean result = interceptor.preHandle(request, response, handler);
//
//        assertTrue(result);
//        assertEquals("GMT", Samay.getTimeZone().getID()); // Default to GMT if header not present
//    }

    // Add more tests as needed to cover different scenarios and edge cases
}

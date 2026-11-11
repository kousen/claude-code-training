package com.kousen.cert.analytics.interceptor;

import com.kousen.cert.analytics.service.AnalyticsService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ApiTrackingInterceptorTest {

    private final AnalyticsService analyticsService = mock(AnalyticsService.class);
    private final ApiTrackingInterceptor interceptor = new ApiTrackingInterceptor(analyticsService);
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @Test
    void shouldTrackFirstForwardedAddressWhenBehindProxy() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/certificates/books");
        request.addHeader("X-Forwarded-For", "203.0.113.7, 10.0.0.1");
        request.addHeader("User-Agent", "curl/8.0");

        interceptor.preHandle(request, response, new Object());
        interceptor.afterCompletion(request, response, new Object(), null);

        verify(analyticsService).trackApiUsageWithExtractedData(
                eq("/api/certificates/books"), anyLong(), eq("203.0.113.7"), eq("curl/8.0"));
    }

    @Test
    void shouldTrackRemoteAddressWhenNotForwarded() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/certificates/books");
        request.setRemoteAddr("198.51.100.4");

        interceptor.preHandle(request, response, new Object());
        interceptor.afterCompletion(request, response, new Object(), null);

        verify(analyticsService).trackApiUsageWithExtractedData(
                eq("/api/certificates/books"), anyLong(), eq("198.51.100.4"), eq(null));
    }

    @Test
    void shouldNotTrackWhenRequestWasNeverTimed() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/certificates/books");

        interceptor.afterCompletion(request, response, new Object(), null);

        verifyNoInteractions(analyticsService);
    }

    @Test
    void shouldNotFailTheRequestWhenTrackingThrows() {
        when(analyticsService.trackApiUsageWithExtractedData(any(), anyLong(), any(), any()))
                .thenThrow(new RuntimeException("analytics down"));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/certificates/books");

        interceptor.preHandle(request, response, new Object());

        assertThatCode(() -> interceptor.afterCompletion(request, response, new Object(), null))
                .doesNotThrowAnyException();
    }
}

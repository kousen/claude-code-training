package com.kousen.cert.analytics.service;

import com.kousen.cert.analytics.model.AnalyticsDTO;
import com.kousen.cert.analytics.model.CertificateEvent;
import com.kousen.cert.analytics.model.CertificateEvent.EventType;
import com.kousen.cert.analytics.model.CertificateMetadata;
import com.kousen.cert.analytics.repository.CertificateEventRepository;
import com.kousen.cert.analytics.repository.CertificateMetadataRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private CertificateEventRepository eventRepository;

    @Mock
    private CertificateMetadataRepository metadataRepository;

    @Mock
    private HttpServletRequest request;

    private MeterRegistry meterRegistry;
    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        analyticsService = new AnalyticsService(eventRepository, metadataRepository, meterRegistry);
    }

    @Test
    void shouldTrackCertificateGenerated() throws Exception {
        // Given
        String certificateId = "test-cert-123";
        String purchaserName = "John Doe";
        String purchaserEmail = "john@example.com";
        String bookTitle = "Modern Java Recipes";
        long durationMs = 1500L;

        when(request.getHeader("User-Agent")).thenReturn("Test Browser");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // When
        CompletableFuture<Void> future = analyticsService.trackCertificateGenerated(
            certificateId, purchaserName, purchaserEmail, bookTitle, durationMs, request
        );
        future.get(); // Wait for async completion

        // Then
        ArgumentCaptor<CertificateEvent> eventCaptor = ArgumentCaptor.forClass(CertificateEvent.class);
        verify(eventRepository).save(eventCaptor.capture());

        CertificateEvent savedEvent = eventCaptor.getValue();
        assertEquals(EventType.GENERATED, savedEvent.getEventType());
        assertEquals(certificateId, savedEvent.getCertificateId());
        assertEquals(purchaserName, savedEvent.getPurchaserName());
        assertEquals(purchaserEmail, savedEvent.getPurchaserEmail());
        assertEquals(bookTitle, savedEvent.getBookTitle());
        assertEquals(durationMs, savedEvent.getDurationMs());
        assertEquals("127.0.0.1", savedEvent.getIpAddress());
        assertEquals("Test Browser", savedEvent.getUserAgent());

        // Verify metrics
        assertEquals(1.0, meterRegistry.counter("certificates.generated", "book", bookTitle).count());
    }

    @Test
    void shouldTrackCertificateVerified() throws Exception {
        // Given
        String certificateId = "test-cert-123";
        CertificateMetadata metadata = new CertificateMetadata(certificateId, "test.pdf");

        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(metadataRepository.findById(certificateId)).thenReturn(Optional.of(metadata));

        // When
        CompletableFuture<Void> future = analyticsService.trackCertificateVerified(certificateId, request);
        future.get();

        // Then
        verify(eventRepository).save(argThat(event -> 
            event.getEventType() == EventType.VERIFIED &&
            event.getCertificateId().equals(certificateId)
        ));

        verify(metadataRepository).save(argThat(m -> 
            m.getVerificationCount() == 1 &&
            m.getLastVerifiedAt() != null
        ));

        assertEquals(1.0, meterRegistry.counter("certificates.verified").count());
    }

    @Test
    void shouldGetDashboardData() {
        // Given
        Instant now = Instant.now();
        Instant weekAgo = now.minus(7, ChronoUnit.DAYS);

        when(metadataRepository.countTotalCertificates()).thenReturn(100L);
        when(eventRepository.countEventsSince(eq(EventType.GENERATED), any())).thenReturn(10L, 50L, 80L);
        when(metadataRepository.sumTotalVerifications()).thenReturn(200L);
        when(eventRepository.countUniquePurchasers()).thenReturn(75L);
        when(eventRepository.findAverageDuration(EventType.GENERATED)).thenReturn(1250.0);

        List<Object[]> bookPopularity = Arrays.asList(
            new Object[]{"Modern Java Recipes", 45L},
            new Object[]{"Spring Boot in Action", 35L}
        );
        when(eventRepository.findBookPopularity(EventType.GENERATED)).thenReturn(bookPopularity);

        List<Object[]> dailyTrend = Arrays.asList(
            new Object[]{java.sql.Date.valueOf("2024-01-01"), 5L},
            new Object[]{java.sql.Date.valueOf("2024-01-02"), 8L}
        );
        when(eventRepository.findDailyEventCounts(eq(EventType.GENERATED), any(), any())).thenReturn(dailyTrend);

        when(eventRepository.findTop10ByOrderByTimestampDesc()).thenReturn(Arrays.asList(
            createTestEvent(EventType.GENERATED, "cert-1", "Alice", "Modern Java Recipes")
        ));

        // When
        AnalyticsDTO.DashboardData dashboard = analyticsService.getDashboardData();

        // Then
        assertNotNull(dashboard);
        assertEquals(100L, dashboard.summary().totalCertificates());
        assertEquals(10L, dashboard.summary().certificatesToday());
        assertEquals(50L, dashboard.summary().certificatesThisWeek());
        assertEquals(80L, dashboard.summary().certificatesThisMonth());
        assertEquals(200L, dashboard.summary().totalVerifications());
        assertEquals(75L, dashboard.summary().uniquePurchasers());
        assertEquals(1250.0, dashboard.summary().averageGenerationTime());
        assertEquals("Modern Java Recipes", dashboard.summary().mostPopularBook());

        assertEquals(2, dashboard.dailyTrend().size());
        assertEquals(2, dashboard.bookPopularity().size());
        assertEquals(1, dashboard.recentActivities().size());

        assertTrue(dashboard.performance().successRate() > 0);
        assertNotNull(dashboard.systemMetrics());
    }

    @Test
    void shouldExtractIpAddressFromXForwardedFor() throws Exception {
        // Given
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.195, 70.41.3.18, 150.172.238.178");

        // When
        CompletableFuture<Void> future = analyticsService.trackCertificateDownloaded("cert-123", request);
        future.get();

        // Then
        verify(eventRepository).save(argThat(event -> 
            "203.0.113.195".equals(event.getIpAddress())
        ));
    }

    @Test
    void shouldTrackApiUsage() throws Exception {
        // Given
        String endpoint = "/api/certificates";
        long responseTime = 250L;

        when(request.getHeader("User-Agent")).thenReturn("Test Browser");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // When
        CompletableFuture<Void> future = analyticsService.trackApiUsage(endpoint, responseTime, request);
        future.get(); // Wait for async completion

        // Then
        ArgumentCaptor<CertificateEvent> eventCaptor = ArgumentCaptor.forClass(CertificateEvent.class);
        verify(eventRepository).save(eventCaptor.capture());

        CertificateEvent savedEvent = eventCaptor.getValue();
        assertEquals(EventType.API_CALL, savedEvent.getEventType());
        assertEquals(endpoint, savedEvent.getEndpoint());
        assertEquals(responseTime, savedEvent.getDurationMs());
        assertEquals("127.0.0.1", savedEvent.getIpAddress());
        assertEquals("Test Browser", savedEvent.getUserAgent());

        // Verify metrics
        assertEquals(1.0, meterRegistry.counter("api.calls", "endpoint", endpoint).count());
    }

    private CertificateEvent createTestEvent(EventType type, String certId, String purchaser, String book) {
        CertificateEvent event = new CertificateEvent(type, certId);
        event.setPurchaserName(purchaser);
        event.setBookTitle(book);
        event.setTimestamp(Instant.now());
        return event;
    }

    @Test
    void shouldReadRequestOnCallingThreadBeforeGoingAsync() throws Exception {
        // The servlet container recycles the request once the response completes,
        // so nothing may touch it from the background task.
        Thread caller = Thread.currentThread();
        java.util.concurrent.atomic.AtomicReference<Thread> reader = new java.util.concurrent.atomic.AtomicReference<>();
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenAnswer(invocation -> {
            reader.set(Thread.currentThread());
            return "Test Browser";
        });

        analyticsService.trackCertificateVerified("cert-1", request).get();
        assertSame(caller, reader.getAndSet(null), "verified");

        analyticsService.trackCertificateDownloaded("cert-1", request).get();
        assertSame(caller, reader.getAndSet(null), "downloaded");

        analyticsService.trackCertificateError("boom", request).get();
        assertSame(caller, reader.getAndSet(null), "error");

        analyticsService.trackCertificateGenerated("cert-1", "Ada", null, "Kotlin Cookbook", 10L, request).get();
        assertSame(caller, reader.getAndSet(null), "generated");
    }
}

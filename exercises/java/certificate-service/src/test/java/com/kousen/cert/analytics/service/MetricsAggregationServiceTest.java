package com.kousen.cert.analytics.service;

import com.kousen.cert.analytics.model.AggregatedMetrics;
import com.kousen.cert.analytics.model.CertificateEvent;
import com.kousen.cert.analytics.model.CertificateEvent.EventType;
import com.kousen.cert.analytics.repository.AggregatedMetricsRepository;
import com.kousen.cert.analytics.repository.CertificateEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricsAggregationServiceTest {

    @Mock
    private CertificateEventRepository eventRepository;

    @Mock
    private AggregatedMetricsRepository metricsRepository;

    @Captor
    private ArgumentCaptor<AggregatedMetrics> metricsCaptor;

    private MetricsAggregationService aggregationService;

    @BeforeEach
    void setUp() {
        aggregationService = new MetricsAggregationService(eventRepository, metricsRepository);
    }

    @Test
    void shouldAggregateDailyMetrics() {
        // Given
        Instant now = Instant.now();
        Instant yesterday = now.minus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
        Instant today = now.truncatedTo(ChronoUnit.DAYS);

        // Mock book popularity data
        List<Object[]> bookPopularity = Arrays.asList(
            new Object[]{"Modern Java Recipes", 10L},
            new Object[]{"Spring Boot in Action", 5L}
        );
        when(eventRepository.findBookPopularityBetween(eq(EventType.GENERATED), any(), any())).thenReturn(bookPopularity);

        // Mock verification count
        when(eventRepository.countEventsSince(eq(EventType.VERIFIED), any())).thenReturn(15L);

        // Mock API events
        CertificateEvent apiEvent1 = new CertificateEvent(EventType.API_CALL, "api-1");
        apiEvent1.setEndpoint("/api/certificates");
        apiEvent1.setDurationMs(100L);

        CertificateEvent apiEvent2 = new CertificateEvent(EventType.API_CALL, "api-2");
        apiEvent2.setEndpoint("/api/certificates");
        apiEvent2.setDurationMs(200L);

        CertificateEvent apiEvent3 = new CertificateEvent(EventType.API_CALL, "api-3");
        apiEvent3.setEndpoint("/api/verify");
        apiEvent3.setDurationMs(150L);

        List<CertificateEvent> apiEvents = Arrays.asList(apiEvent1, apiEvent2, apiEvent3);
        when(eventRepository.findByEventTypeAndTimestampBetween(eq(EventType.API_CALL), any(), any()))
            .thenReturn(apiEvents);

        // Mock error count
        when(eventRepository.countEventsSince(eq(EventType.FAILED), any())).thenReturn(2L);

        // When
        aggregationService.aggregateDailyMetrics();

        // Then
        // Verify book popularity metrics were saved
        verify(metricsRepository, times(2)).save(argThat(metric -> 
            metric.getMetricName().equals("certificate_generation_count") &&
            metric.getTimeFrame().equals("daily")
        ));

        // Verify verification count metric was saved
        verify(metricsRepository).save(argThat(metric -> 
            metric.getMetricName().equals("certificate_verification_count") &&
            metric.getMetricValue() == 15.0 &&
            metric.getTimeFrame().equals("daily")
        ));

        // Verify API usage metrics were saved
        verify(metricsRepository).save(argThat(metric -> 
            metric.getMetricName().equals("api_usage_count") &&
            metric.getMetricKey().equals("/api/certificates") &&
            metric.getMetricValue() == 2.0 &&
            metric.getTimeFrame().equals("daily")
        ));

        verify(metricsRepository).save(argThat(metric -> 
            metric.getMetricName().equals("api_usage_count") &&
            metric.getMetricKey().equals("/api/verify") &&
            metric.getMetricValue() == 1.0 &&
            metric.getTimeFrame().equals("daily")
        ));

        // Verify API response time metrics were saved
        verify(metricsRepository).save(argThat(metric -> 
            metric.getMetricName().equals("api_avg_response_time") &&
            metric.getMetricKey().equals("/api/certificates") &&
            metric.getMetricValue() == 150.0 &&
            metric.getTimeFrame().equals("daily")
        ));

        verify(metricsRepository).save(argThat(metric -> 
            metric.getMetricName().equals("api_avg_response_time") &&
            metric.getMetricKey().equals("/api/verify") &&
            metric.getMetricValue() == 150.0 &&
            metric.getTimeFrame().equals("daily")
        ));

        // Verify error count metric was saved
        verify(metricsRepository).save(argThat(metric -> 
            metric.getMetricName().equals("error_count") &&
            metric.getMetricValue() == 2.0 &&
            metric.getTimeFrame().equals("daily")
        ));

        // Verify old metrics were cleaned up
        verify(metricsRepository).deleteByCalculatedAtBefore(any());
    }

    @Test
    void shouldAggregateWeeklyMetrics() {
        // Given
        Instant now = Instant.now();
        Instant weekAgo = now.minus(7, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
        Instant today = now.truncatedTo(ChronoUnit.DAYS);

        // Mock book popularity data
        Object[] bookData = new Object[]{"Modern Java Recipes", 50L};
        List<Object[]> bookPopularity = Collections.singletonList(bookData);
        when(eventRepository.findBookPopularityBetween(eq(EventType.GENERATED), any(), any())).thenReturn(bookPopularity);

        // Mock verification count
        when(eventRepository.countEventsSince(eq(EventType.VERIFIED), any())).thenReturn(30L);

        // Mock API events (empty list for simplicity)
        when(eventRepository.findByEventTypeAndTimestampBetween(eq(EventType.API_CALL), any(), any()))
            .thenReturn(Collections.emptyList());

        // Mock error count
        when(eventRepository.countEventsSince(eq(EventType.FAILED), any())).thenReturn(5L);

        // When
        aggregationService.aggregateWeeklyMetrics();

        // Then
        // Verify book popularity metrics were saved with weekly timeframe
        verify(metricsRepository).save(argThat(metric -> 
            metric.getMetricName().equals("certificate_generation_count") &&
            metric.getTimeFrame().equals("weekly")
        ));

        // Verify verification count metric was saved with weekly timeframe
        verify(metricsRepository).save(argThat(metric -> 
            metric.getMetricName().equals("certificate_verification_count") &&
            metric.getTimeFrame().equals("weekly")
        ));

        // Verify error count metric was saved with weekly timeframe
        verify(metricsRepository).save(argThat(metric -> 
            metric.getMetricName().equals("error_count") &&
            metric.getTimeFrame().equals("weekly")
        ));
    }
}
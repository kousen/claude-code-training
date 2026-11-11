package com.kousen.cert.analytics.service;

import com.kousen.cert.analytics.model.AggregatedMetrics;
import com.kousen.cert.analytics.model.CertificateEvent;
import com.kousen.cert.analytics.model.CertificateEvent.EventType;
import com.kousen.cert.analytics.repository.AggregatedMetricsRepository;
import com.kousen.cert.analytics.repository.CertificateEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs the aggregation against a real (H2) database so the date range in the query is exercised.
 */
@DataJpaTest
@Import(MetricsAggregationService.class)
class MetricsAggregationDateRangeTest {

    @Autowired
    private CertificateEventRepository eventRepository;

    @Autowired
    private AggregatedMetricsRepository metricsRepository;

    @Autowired
    private MetricsAggregationService aggregationService;

    @Test
    void shouldCountOnlyCertificatesGeneratedYesterdayInDailyMetrics() {
        Instant today = Instant.now().truncatedTo(ChronoUnit.DAYS);
        saveGenerated("Kotlin Cookbook", today.minus(12, ChronoUnit.HOURS));
        saveGenerated("Kotlin Cookbook", today.minus(10, ChronoUnit.DAYS));
        saveGenerated("Mockito Made Clear", today.minus(10, ChronoUnit.DAYS));

        aggregationService.aggregateDailyMetrics();

        assertThat(metricsRepository.findAll())
                .filteredOn(metric -> metric.getMetricName().equals("certificate_generation_count"))
                .singleElement()
                .satisfies(metric -> {
                    assertThat(metric.getMetricKey()).isEqualTo("Kotlin Cookbook");
                    assertThat(metric.getTimeFrame()).isEqualTo("daily");
                    assertThat(metric.getMetricValue()).isEqualTo(1.0);
                });
    }

    private void saveGenerated(String bookTitle, Instant timestamp) {
        CertificateEvent event = new CertificateEvent(EventType.GENERATED, "cert-" + timestamp.toEpochMilli() + bookTitle.length());
        event.setBookTitle(bookTitle);
        event.setTimestamp(timestamp);
        eventRepository.save(event);
    }
}

package com.kousen.cert.analytics.service;

import com.kousen.cert.analytics.model.AggregatedMetrics;
import com.kousen.cert.analytics.model.CertificateEvent;
import com.kousen.cert.analytics.repository.AggregatedMetricsRepository;
import com.kousen.cert.analytics.repository.CertificateEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for aggregating metrics data and storing it in the aggregated_metrics table.
 * This service runs scheduled tasks to calculate and store aggregated metrics.
 */
@Service
public class MetricsAggregationService {
    private static final Logger logger = LoggerFactory.getLogger(MetricsAggregationService.class);

    private final CertificateEventRepository eventRepository;
    private final AggregatedMetricsRepository metricsRepository;

    @Value("${analytics.metrics.retention-days:30}")
    private int metricsRetentionDays;

    public MetricsAggregationService(CertificateEventRepository eventRepository,
                                    AggregatedMetricsRepository metricsRepository) {
        this.eventRepository = eventRepository;
        this.metricsRepository = metricsRepository;
    }

    /**
     * Scheduled task to aggregate daily metrics.
     * Runs at 1:00 AM every day.
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void aggregateDailyMetrics() {
        logger.info("Starting daily metrics aggregation");
        Instant now = Instant.now();
        Instant yesterday = now.minus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
        Instant today = now.truncatedTo(ChronoUnit.DAYS);

        try {
            // Aggregate certificate generation counts by book
            aggregateCertificateGenerationByBook(yesterday, today);

            // Aggregate certificate verification counts
            aggregateCertificateVerifications(yesterday, today);

            // Aggregate API usage by endpoint
            aggregateApiUsageByEndpoint(yesterday, today);

            // Aggregate error counts
            aggregateErrorCounts(yesterday, today);

            // Clean up old metrics
            cleanupOldMetrics();

            logger.info("Daily metrics aggregation completed successfully");
        } catch (Exception e) {
            logger.error("Error during daily metrics aggregation", e);
        }
    }

    /**
     * Scheduled task to aggregate weekly metrics.
     * Runs at 2:00 AM every Monday.
     */
    @Scheduled(cron = "0 0 2 ? * MON")
    @Transactional
    public void aggregateWeeklyMetrics() {
        logger.info("Starting weekly metrics aggregation");
        Instant now = Instant.now();
        Instant weekAgo = now.minus(7, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
        Instant today = now.truncatedTo(ChronoUnit.DAYS);

        try {
            // Aggregate certificate generation counts by book
            aggregateCertificateGenerationByBook(weekAgo, today, "weekly");

            // Aggregate certificate verification counts
            aggregateCertificateVerifications(weekAgo, today, "weekly");

            // Aggregate API usage by endpoint
            aggregateApiUsageByEndpoint(weekAgo, today, "weekly");

            // Aggregate error counts
            aggregateErrorCounts(weekAgo, today, "weekly");

            logger.info("Weekly metrics aggregation completed successfully");
        } catch (Exception e) {
            logger.error("Error during weekly metrics aggregation", e);
        }
    }

    /**
     * Scheduled task to aggregate monthly metrics.
     * Runs at 3:00 AM on the 1st day of each month.
     */
    @Scheduled(cron = "0 0 3 1 * ?")
    @Transactional
    public void aggregateMonthlyMetrics() {
        logger.info("Starting monthly metrics aggregation");
        Instant now = Instant.now();
        Instant monthAgo = now.minus(30, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
        Instant today = now.truncatedTo(ChronoUnit.DAYS);

        try {
            // Aggregate certificate generation counts by book
            aggregateCertificateGenerationByBook(monthAgo, today, "monthly");

            // Aggregate certificate verification counts
            aggregateCertificateVerifications(monthAgo, today, "monthly");

            // Aggregate API usage by endpoint
            aggregateApiUsageByEndpoint(monthAgo, today, "monthly");

            // Aggregate error counts
            aggregateErrorCounts(monthAgo, today, "monthly");

            logger.info("Monthly metrics aggregation completed successfully");
        } catch (Exception e) {
            logger.error("Error during monthly metrics aggregation", e);
        }
    }

    /**
     * Aggregate certificate generation counts by book.
     */
    private void aggregateCertificateGenerationByBook(Instant start, Instant end) {
        aggregateCertificateGenerationByBook(start, end, "daily");
    }

    /**
     * Aggregate certificate generation counts by book with specified time frame.
     */
    private void aggregateCertificateGenerationByBook(Instant start, Instant end, String timeFrame) {
        List<Object[]> results = eventRepository.findBookPopularityBetween(
                CertificateEvent.EventType.GENERATED, start, end);
        
        for (Object[] result : results) {
            String bookTitle = (String) result[0];
            Long count = (Long) result[1];
            
            AggregatedMetrics metric = new AggregatedMetrics(
                "certificate_generation_count",
                bookTitle,
                timeFrame,
                count.doubleValue(),
                end
            );
            
            metricsRepository.save(metric);
        }
    }

    /**
     * Aggregate certificate verification counts.
     */
    private void aggregateCertificateVerifications(Instant start, Instant end) {
        aggregateCertificateVerifications(start, end, "daily");
    }

    /**
     * Aggregate certificate verification counts with specified time frame.
     */
    private void aggregateCertificateVerifications(Instant start, Instant end, String timeFrame) {
        long count = eventRepository.countEventsSince(CertificateEvent.EventType.VERIFIED, start);
        
        AggregatedMetrics metric = new AggregatedMetrics(
            "certificate_verification_count",
            "all",
            timeFrame,
            (double) count,
            end
        );
        
        metricsRepository.save(metric);
    }

    /**
     * Aggregate API usage by endpoint.
     */
    private void aggregateApiUsageByEndpoint(Instant start, Instant end) {
        aggregateApiUsageByEndpoint(start, end, "daily");
    }

    /**
     * Aggregate API usage by endpoint with specified time frame.
     */
    private void aggregateApiUsageByEndpoint(Instant start, Instant end, String timeFrame) {
        List<CertificateEvent> apiEvents = eventRepository.findByEventTypeAndTimestampBetween(
            CertificateEvent.EventType.API_CALL, start, end);
        
        Map<String, Long> endpointCounts = apiEvents.stream()
            .filter(event -> event.getEndpoint() != null)
            .collect(Collectors.groupingBy(
                CertificateEvent::getEndpoint,
                Collectors.counting()
            ));
        
        for (Map.Entry<String, Long> entry : endpointCounts.entrySet()) {
            AggregatedMetrics metric = new AggregatedMetrics(
                "api_usage_count",
                entry.getKey(),
                timeFrame,
                entry.getValue().doubleValue(),
                end
            );
            
            metricsRepository.save(metric);
        }
        
        // Calculate average response time by endpoint
        Map<String, Double> endpointAvgTimes = apiEvents.stream()
            .filter(event -> event.getEndpoint() != null && event.getDurationMs() != null)
            .collect(Collectors.groupingBy(
                CertificateEvent::getEndpoint,
                Collectors.averagingLong(CertificateEvent::getDurationMs)
            ));
        
        for (Map.Entry<String, Double> entry : endpointAvgTimes.entrySet()) {
            AggregatedMetrics metric = new AggregatedMetrics(
                "api_avg_response_time",
                entry.getKey(),
                timeFrame,
                entry.getValue(),
                end
            );
            
            metricsRepository.save(metric);
        }
    }

    /**
     * Aggregate error counts.
     */
    private void aggregateErrorCounts(Instant start, Instant end) {
        aggregateErrorCounts(start, end, "daily");
    }

    /**
     * Aggregate error counts with specified time frame.
     */
    private void aggregateErrorCounts(Instant start, Instant end, String timeFrame) {
        long count = eventRepository.countEventsSince(CertificateEvent.EventType.FAILED, start);
        
        AggregatedMetrics metric = new AggregatedMetrics(
            "error_count",
            "all",
            timeFrame,
            (double) count,
            end
        );
        
        metricsRepository.save(metric);
    }

    /**
     * Clean up old metrics based on retention policy.
     */
    private void cleanupOldMetrics() {
        Instant cutoffDate = Instant.now().minus(metricsRetentionDays, ChronoUnit.DAYS);
        metricsRepository.deleteByCalculatedAtBefore(cutoffDate);
        logger.info("Cleaned up metrics older than {}", cutoffDate);
    }
}
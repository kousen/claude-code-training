package com.kousen.cert.analytics.service;

import com.kousen.cert.analytics.model.*;
import com.kousen.cert.analytics.model.CertificateEvent.EventType;
import com.kousen.cert.analytics.repository.CertificateEventRepository;
import com.kousen.cert.analytics.repository.CertificateMetadataRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Transactional
public class AnalyticsService {
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);

    private final CertificateEventRepository eventRepository;
    private final CertificateMetadataRepository metadataRepository;
    private final MeterRegistry meterRegistry;

    public AnalyticsService(CertificateEventRepository eventRepository,
                          CertificateMetadataRepository metadataRepository,
                          MeterRegistry meterRegistry) {
        this.eventRepository = eventRepository;
        this.metadataRepository = metadataRepository;
        this.meterRegistry = meterRegistry;
    }

    public CompletableFuture<Void> trackCertificateGenerated(String certificateId, 
                                                            String purchaserName,
                                                            String purchaserEmail,
                                                            String bookTitle,
                                                            long durationMs,
                                                            HttpServletRequest request) {
        ClientInfo client = clientInfo(request);
        return CompletableFuture.runAsync(() -> {
            try {
                CertificateEvent event = new CertificateEvent(EventType.GENERATED, certificateId);
                event.setPurchaserName(purchaserName);
                event.setPurchaserEmail(purchaserEmail);
                event.setBookTitle(bookTitle);
                event.setDurationMs(durationMs);
                event.setIpAddress(client.ipAddress());
                event.setUserAgent(client.userAgent());

                eventRepository.save(event);

                // Update metrics
                meterRegistry.counter("certificates.generated", "book", bookTitle).increment();
                meterRegistry.timer("certificates.generation.time").record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);

                logger.info("Tracked certificate generation: {} for {}", certificateId, purchaserName);
            } catch (Exception e) {
                logger.error("Error tracking certificate generation", e);
            }
        });
    }

    public CompletableFuture<Void> trackCertificateDownloaded(String certificateId, HttpServletRequest request) {
        ClientInfo client = clientInfo(request);
        return CompletableFuture.runAsync(() -> {
            try {
                CertificateEvent event = new CertificateEvent(EventType.DOWNLOADED, certificateId);
                event.setIpAddress(client.ipAddress());
                event.setUserAgent(client.userAgent());

                eventRepository.save(event);
                meterRegistry.counter("certificates.downloaded").increment();

                logger.info("Tracked certificate download: {}", certificateId);
            } catch (Exception e) {
                logger.error("Error tracking certificate download", e);
            }
        });
    }

    public CompletableFuture<Void> trackCertificateVerified(String certificateId, HttpServletRequest request) {
        ClientInfo client = clientInfo(request);
        return CompletableFuture.runAsync(() -> {
            try {
                CertificateEvent event = new CertificateEvent(EventType.VERIFIED, certificateId);
                event.setIpAddress(client.ipAddress());
                event.setUserAgent(client.userAgent());

                eventRepository.save(event);

                // Update metadata
                metadataRepository.findById(certificateId).ifPresent(metadata -> {
                    metadata.incrementVerificationCount();
                    metadataRepository.save(metadata);
                });

                meterRegistry.counter("certificates.verified").increment();

                logger.info("Tracked certificate verification: {}", certificateId);
            } catch (Exception e) {
                logger.error("Error tracking certificate verification", e);
            }
        });
    }

    public CompletableFuture<Void> trackCertificateError(String errorMessage, HttpServletRequest request) {
        ClientInfo client = clientInfo(request);
        return CompletableFuture.runAsync(() -> {
            try {
                CertificateEvent event = new CertificateEvent(EventType.FAILED, UUID.randomUUID().toString());
                event.setErrorMessage(errorMessage);
                event.setIpAddress(client.ipAddress());
                event.setUserAgent(client.userAgent());

                eventRepository.save(event);
                meterRegistry.counter("certificates.errors").increment();

                logger.info("Tracked certificate error: {}", errorMessage);
            } catch (Exception e) {
                logger.error("Error tracking certificate error", e);
            }
        });
    }

    /**
     * Track API usage.
     *
     * @param endpoint     The API endpoint
     * @param responseTime The response time in milliseconds
     * @param request      The HTTP request
     */
    public CompletableFuture<Void> trackApiUsage(String endpoint, long responseTime, HttpServletRequest request) {
        ClientInfo client = clientInfo(request);
        return CompletableFuture.runAsync(() -> {
            try {
                CertificateEvent event = new CertificateEvent(EventType.API_CALL, UUID.randomUUID().toString());
                event.setEndpoint(endpoint);
                event.setDurationMs(responseTime);
                event.setIpAddress(client.ipAddress());
                event.setUserAgent(client.userAgent());

                eventRepository.save(event);

                // Update metrics
                meterRegistry.counter("api.calls", "endpoint", endpoint).increment();
                meterRegistry.timer("api.response.time", "endpoint", endpoint)
                    .record(responseTime, java.util.concurrent.TimeUnit.MILLISECONDS);

                logger.debug("Tracked API usage: {} ({}ms)", endpoint, responseTime);
            } catch (Exception e) {
                logger.error("Error tracking API usage", e);
            }
        });
    }
    
    /**
     * Track API usage with pre-extracted data to avoid request recycling issues.
     *
     * @param endpoint     The API endpoint
     * @param responseTime The response time in milliseconds
     * @param ipAddress    Pre-extracted IP address
     * @param userAgent    Pre-extracted user agent
     */
    @Async
    public CompletableFuture<Void> trackApiUsageWithExtractedData(String endpoint, long responseTime, 
                                                                  String ipAddress, String userAgent) {
        return CompletableFuture.runAsync(() -> {
            try {
                CertificateEvent event = new CertificateEvent(EventType.API_CALL, UUID.randomUUID().toString());
                event.setEndpoint(endpoint);
                event.setDurationMs(responseTime);
                event.setIpAddress(ipAddress);
                event.setUserAgent(userAgent);

                eventRepository.save(event);

                // Update metrics
                meterRegistry.counter("api.calls", "endpoint", endpoint).increment();
                meterRegistry.timer("api.response.time", "endpoint", endpoint)
                    .record(responseTime, java.util.concurrent.TimeUnit.MILLISECONDS);

                logger.debug("Tracked API usage: {} ({}ms)", endpoint, responseTime);
            } catch (Exception e) {
                logger.error("Error tracking API usage", e);
            }
        });
    }

    public AnalyticsDTO.DashboardData getDashboardData() {
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            Instant now = Instant.now();
            Instant today = now.truncatedTo(ChronoUnit.DAYS);
            Instant weekAgo = today.minus(7, ChronoUnit.DAYS);
            Instant monthAgo = today.minus(30, ChronoUnit.DAYS);

            // Build summary
            AnalyticsDTO.Summary summary = new AnalyticsDTO.Summary(
                metadataRepository.countTotalCertificates(),
                eventRepository.countEventsSince(EventType.GENERATED, today),
                eventRepository.countEventsSince(EventType.GENERATED, weekAgo),
                eventRepository.countEventsSince(EventType.GENERATED, monthAgo),
                Optional.ofNullable(metadataRepository.sumTotalVerifications()).orElse(0L),
                eventRepository.countUniquePurchasers(),
                Optional.ofNullable(eventRepository.findAverageDuration(EventType.GENERATED)).orElse(0.0),
                findMostPopularBook()
            );

            // Get daily trend
            List<AnalyticsDTO.TimeSeriesData> dailyTrend = getDailyTrend(weekAgo, now);

            // Get book popularity
            List<AnalyticsDTO.BookPopularity> bookPopularity = getBookPopularity();

            // Get recent activities
            List<AnalyticsDTO.RecentActivity> recentActivities = getRecentActivities();

            // Get performance metrics
            AnalyticsDTO.PerformanceMetrics performance = getPerformanceMetrics();

            // Get system metrics
            Map<String, Object> systemMetrics = getSystemMetrics();

            return new AnalyticsDTO.DashboardData(
                summary,
                dailyTrend,
                bookPopularity,
                recentActivities,
                performance,
                systemMetrics
            );
        } finally {
            sample.stop(meterRegistry.timer("analytics.dashboard.load"));
        }
    }

    /** What the tracking methods need from a request, copied before the request can be recycled. */
    private record ClientInfo(String ipAddress, String userAgent) {}

    /**
     * Reads the request on the calling thread. The container recycles the request when the
     * response completes, so background tasks must not touch it. Never throws: analytics
     * must not fail the request it is recording.
     */
    private ClientInfo clientInfo(HttpServletRequest request) {
        try {
            return new ClientInfo(extractIpAddress(request), request.getHeader("User-Agent"));
        } catch (RuntimeException e) {
            logger.warn("Could not read client details from request: {}", e.getMessage());
            return new ClientInfo(null, null);
        }
    }

    private String extractIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String findMostPopularBook() {
        List<Object[]> results = eventRepository.findBookPopularity(EventType.GENERATED);
        if (!results.isEmpty()) {
            return (String) results.get(0)[0];
        }
        return "None";
    }

    private List<AnalyticsDTO.TimeSeriesData> getDailyTrend(Instant start, Instant end) {
        List<Object[]> results = eventRepository.findDailyEventCounts(EventType.GENERATED, start, end);
        return results.stream()
            .map(row -> new AnalyticsDTO.TimeSeriesData(
                ((java.sql.Date) row[0]).toLocalDate().atStartOfDay().toInstant(java.time.ZoneOffset.UTC),
                (Long) row[1]
            ))
            .collect(Collectors.toList());
    }

    private List<AnalyticsDTO.BookPopularity> getBookPopularity() {
        List<Object[]> results = eventRepository.findBookPopularity(EventType.GENERATED);
        long total = results.stream().mapToLong(row -> (Long) row[1]).sum();

        return results.stream()
            .map(row -> new AnalyticsDTO.BookPopularity(
                (String) row[0],
                (Long) row[1],
                total > 0 ? ((Long) row[1] * 100.0) / total : 0
            ))
            .collect(Collectors.toList());
    }

    private List<AnalyticsDTO.RecentActivity> getRecentActivities() {
        return eventRepository.findTop10ByOrderByTimestampDesc().stream()
            .map(event -> new AnalyticsDTO.RecentActivity(
                event.getCertificateId(),
                event.getPurchaserName() != null ? event.getPurchaserName() : "Anonymous",
                event.getBookTitle() != null ? event.getBookTitle() : "N/A",
                event.getTimestamp(),
                event.getEventType().toString()
            ))
            .collect(Collectors.toList());
    }

    private AnalyticsDTO.PerformanceMetrics getPerformanceMetrics() {
        long successCount = eventRepository.countEventsSince(EventType.GENERATED, Instant.now().minus(24, ChronoUnit.HOURS));
        long failureCount = eventRepository.countEventsSince(EventType.FAILED, Instant.now().minus(24, ChronoUnit.HOURS));
        double successRate = (successCount + failureCount) > 0 ? 
            (successCount * 100.0) / (successCount + failureCount) : 100.0;

        return new AnalyticsDTO.PerformanceMetrics(
            Optional.ofNullable(eventRepository.findAverageDuration(EventType.GENERATED)).orElse(0.0),
            0.0, // Would need to track signing time separately
            Optional.ofNullable(eventRepository.findAverageDuration(EventType.GENERATED)).orElse(0.0),
            successCount,
            failureCount,
            successRate
        );
    }

    private Map<String, Object> getSystemMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalMemory", Runtime.getRuntime().totalMemory() / (1024 * 1024));
        metrics.put("freeMemory", Runtime.getRuntime().freeMemory() / (1024 * 1024));
        metrics.put("maxMemory", Runtime.getRuntime().maxMemory() / (1024 * 1024));
        metrics.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        return metrics;
    }
}

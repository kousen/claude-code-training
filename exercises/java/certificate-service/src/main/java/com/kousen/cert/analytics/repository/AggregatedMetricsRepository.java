package com.kousen.cert.analytics.repository;

import com.kousen.cert.analytics.model.AggregatedMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

/**
 * Repository for accessing and manipulating AggregatedMetrics entities.
 */
@Repository
public interface AggregatedMetricsRepository extends JpaRepository<AggregatedMetrics, Long> {
    
    /**
     * Delete metrics older than the specified date.
     */
    void deleteByCalculatedAtBefore(Instant date);
}
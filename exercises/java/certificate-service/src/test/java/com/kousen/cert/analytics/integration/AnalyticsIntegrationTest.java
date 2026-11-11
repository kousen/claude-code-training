package com.kousen.cert.analytics.integration;

import com.kousen.cert.analytics.model.AnalyticsDTO;
import com.kousen.cert.analytics.model.CertificateEvent;
import com.kousen.cert.analytics.repository.CertificateEventRepository;
import com.kousen.cert.analytics.repository.CertificateMetadataRepository;
import com.kousen.cert.analytics.service.AnalyticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class AnalyticsIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private CertificateEventRepository eventRepository;
    
    @Autowired
    private CertificateMetadataRepository metadataRepository;
    
    @Autowired
    private AnalyticsService analyticsService;
    
    @Test
    void shouldRetrieveAnalyticsDashboard() {
        // Given - seed some data
        CertificateEvent event = new CertificateEvent(CertificateEvent.EventType.GENERATED, "test-123");
        event.setPurchaserName("Test User");
        event.setBookTitle("Modern Java Recipes");
        event.setDurationMs(1000L);
        eventRepository.save(event);
        
        // When
        ResponseEntity<AnalyticsDTO.DashboardData> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/analytics/dashboard",
            AnalyticsDTO.DashboardData.class
        );
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().summary().totalCertificates() >= 0);
        assertNotNull(response.getBody().dailyTrend());
        assertNotNull(response.getBody().bookPopularity());
    }
    
    @Test
    void shouldRetrieveAnalyticsSummary() {
        // When
        ResponseEntity<AnalyticsDTO.Summary> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/analytics/summary",
            AnalyticsDTO.Summary.class
        );
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().totalCertificates() >= 0);
        assertTrue(response.getBody().uniquePurchasers() >= 0);
    }
    
    @Test
    void shouldPersistEventsToDatabase() {
        // Given
        long initialCount = eventRepository.count();
        
        // When
        CertificateEvent event = new CertificateEvent(CertificateEvent.EventType.GENERATED, "test-456");
        event.setPurchaserName("Jane Doe");
        event.setPurchaserEmail("jane@example.com");
        event.setBookTitle("Spring Boot in Action");
        eventRepository.save(event);
        
        // Then
        assertEquals(initialCount + 1, eventRepository.count());
        assertTrue(eventRepository.findByCertificateId("test-456").size() > 0);
    }
}
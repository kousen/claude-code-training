package com.kousen.cert.analytics.controller;

import com.kousen.cert.analytics.model.AnalyticsDTO;
import com.kousen.cert.analytics.service.AnalyticsService;
import com.kousen.cert.service.PdfService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest({AnalyticsController.class, AnalyticsDashboardController.class})
@ActiveProfiles("test")
class AnalyticsControllerTest {

    private static final AnalyticsDTO.DashboardData DASHBOARD = new AnalyticsDTO.DashboardData(
            new AnalyticsDTO.Summary(42, 3, 10, 20, 7, 30, 123.5, "Kotlin Cookbook"),
            List.of(new AnalyticsDTO.TimeSeriesData(Instant.parse("2026-11-10T00:00:00Z"), 5)),
            List.of(new AnalyticsDTO.BookPopularity("Kotlin Cookbook", 25, 59.5)),
            List.of(new AnalyticsDTO.RecentActivity("cert-1", "Ada Lovelace", "Kotlin Cookbook",
                    Instant.parse("2026-11-10T12:00:00Z"), "GENERATED")),
            new AnalyticsDTO.PerformanceMetrics(100.0, 50.0, 150.0, 40, 2, 95.2),
            Map.of("totalMemory", 512L, "freeMemory", 256L, "maxMemory", 1024L, "availableProcessors", 8));

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyticsService analyticsService;

    // CertificateConfig is loaded by the web slice and needs a PdfService
    @MockitoBean
    private PdfService pdfService;

    @BeforeEach
    void stubDashboardData() {
        when(analyticsService.getDashboardData()).thenReturn(DASHBOARD);
    }

    @ParameterizedTest
    @CsvSource({
            "/api/analytics/dashboard,   $.summary.mostPopularBook,  Kotlin Cookbook",
            "/api/analytics/summary,     $.totalCertificates,        42",
            "/api/analytics/trends,      $[0].count,                 5",
            "/api/analytics/books,       $[0].bookTitle,             Kotlin Cookbook",
            "/api/analytics/performance, $.successRate,              95.2"
    })
    void shouldExposeEachSliceOfTheDashboardData(String path, String jsonPath, String expected) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().isOk())
                .andExpect(jsonPath(jsonPath).value(org.hamcrest.Matchers.hasToString(expected)));
    }

    @Test
    void shouldRenderDashboardPageWithData() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("analytics/dashboard"))
                .andExpect(model().attribute("dashboardData", DASHBOARD))
                .andExpect(content().string(containsString("Kotlin Cookbook")));
    }
}

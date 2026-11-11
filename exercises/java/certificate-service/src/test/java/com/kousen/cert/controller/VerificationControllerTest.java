package com.kousen.cert.controller;

import com.kousen.cert.analytics.service.AnalyticsService;
import com.kousen.cert.service.KeyStoreProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.ExtendedModelMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "certificate.keystore=src/test/resources/keystores/test-keystore.p12"
})
class VerificationControllerTest {

    // SHA-256 fingerprint: 32 hex pairs separated by colons
    private static final String FINGERPRINT_PATTERN = "([0-9A-F]{2}:){31}[0-9A-F]{2}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyticsService analyticsService;

    @Test
    void shouldShowDefaultsAndRealFingerprintWhenNoParameters() throws Exception {
        mockMvc.perform(get("/verify-certificate"))
                .andExpect(status().isOk())
                .andExpect(view().name("verify-certificate"))
                .andExpect(model().attribute("name", "Not specified"))
                .andExpect(model().attribute("bookTitle", "Not specified"))
                .andExpect(model().attribute("issueDate", "Not specified"))
                .andExpect(model().attribute("certificateFingerprint",
                        matchesPattern(FINGERPRINT_PATTERN)));
    }

    @Test
    void shouldReturnVerificationPageWithProvidedParameters() throws Exception {
        mockMvc.perform(get("/verify-certificate")
                    .param("name", "John Doe")
                    .param("book", "Modern Java Recipes")
                    .param("date", "2025-04-18"))
                .andExpect(status().isOk())
                .andExpect(view().name("verify-certificate"))
                .andExpect(model().attribute("name", "John Doe"))
                .andExpect(model().attribute("bookTitle", "Modern Java Recipes"))
                .andExpect(model().attribute("issueDate", "2025-04-18"));
    }

    @Test
    void shouldTrackVerificationWhenCertificateIdProvided() throws Exception {
        mockMvc.perform(get("/verify-certificate").param("id", "cert-123"))
                .andExpect(status().isOk());

        verify(analyticsService).trackCertificateVerified(eq("cert-123"), any());
    }

    @Test
    void shouldNotTrackVerificationWhenCertificateIdMissingOrEmpty() throws Exception {
        mockMvc.perform(get("/verify-certificate")).andExpect(status().isOk());
        mockMvc.perform(get("/verify-certificate").param("id", "")).andExpect(status().isOk());

        verify(analyticsService, never()).trackCertificateVerified(any(), any());
    }

    @Test
    void shouldStillRenderPageWhenTrackingFails() throws Exception {
        when(analyticsService.trackCertificateVerified(any(), any()))
                .thenThrow(new RuntimeException("analytics down"));

        mockMvc.perform(get("/verify-certificate").param("id", "cert-123"))
                .andExpect(status().isOk())
                .andExpect(view().name("verify-certificate"));
    }

    @Test
    void shouldFallBackWhenCertificateCannotBeRead() {
        KeyStoreProvider brokenProvider = mock(KeyStoreProvider.class);
        when(brokenProvider.keyStore()).thenThrow(new IllegalStateException("keystore unavailable"));
        VerificationController controller =
                new VerificationController(brokenProvider, mock(AnalyticsService.class));
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.verifyPage(null, null, null, null, model, null);

        assertThat(view).isEqualTo("verify-certificate");
        assertThat(model.get("certificateFingerprint"))
                .isEqualTo("Error generating certificate fingerprint");
    }
}

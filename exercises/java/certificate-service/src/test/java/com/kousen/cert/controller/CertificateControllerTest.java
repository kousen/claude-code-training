package com.kousen.cert.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kousen.cert.analytics.service.AnalyticsService;
import com.kousen.cert.analytics.service.CertificateMetadataService;
import com.kousen.cert.model.CertificateRequest;
import com.kousen.cert.service.CertificateStorageService;
import com.kousen.cert.service.PdfService;
import com.kousen.cert.analytics.model.CertificateMetadata;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CertificateController.class)
@org.springframework.test.context.ActiveProfiles("test")
class CertificateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PdfService pdfService;
    
    @MockitoBean
    private CertificateStorageService storageService;
    
    @MockitoBean
    private AnalyticsService analyticsService;
    
    @MockitoBean
    private CertificateMetadataService metadataService;

    @Test
    void shouldCreateCertificateAndReturnPdf() throws Exception {
        // Given
        CertificateRequest request = new CertificateRequest(
                "Adm. Grace Hopper",
                "Making Java Groovy",
                Optional.of("grace@example.com")
        );

        // Create a temporary real PDF file for the mock using PDFBox
        Path tempPdf = Files.createTempFile("test-cert-", ".pdf");
        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage());
            doc.save(tempPdf.toFile());
        }

        when(pdfService.createPdf(any())).thenReturn(tempPdf);
        when(storageService.storeCertificate(any(), any())).thenReturn(tempPdf);

        // When/Then
        byte[] body = mockMvc.perform(post("/api/certificates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(header().exists("X-Certificate-Id"))
                .andReturn().getResponse().getContentAsByteArray();

        // The returned PDF carries a digital signature
        try (PDDocument returned = Loader.loadPDF(body)) {
            assertThat(returned.getSignatureDictionaries()).isNotEmpty();
        }

        verify(analyticsService).trackCertificateGenerated(anyString(), eq("Adm. Grace Hopper"),
                eq("grace@example.com"), eq("Making Java Groovy"), anyLong(), any());
        verify(metadataService).saveCertificateMetadata(anyString(), eq(tempPdf));

        // Clean up
        Files.deleteIfExists(tempPdf);
    }

    @Test
    void shouldRejectBlankPurchaserName() throws Exception {
        String requestJson = """
                {"purchaserName": "  ", "bookTitle": "Kotlin Cookbook"}
                """;

        mockMvc.perform(post("/api/certificates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.purchaserName").value("Purchaser name is required"));

        verify(pdfService, never()).createPdf(any());
    }

    @Test
    void shouldRejectMalformedEmail() throws Exception {
        String requestJson = """
                {"purchaserName": "Test User", "bookTitle": "Kotlin Cookbook", "purchaserEmail": "not-an-email"}
                """;

        mockMvc.perform(post("/api/certificates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.purchaserEmail").value("Invalid email format"));

        verify(pdfService, never()).createPdf(any());
    }

    @Test
    void shouldTrackErrorWhenPdfGenerationFails() throws Exception {
        when(pdfService.createPdf(any())).thenThrow(new IOException("font exploded"));
        String requestJson = """
                {"purchaserName": "Test User", "bookTitle": "Kotlin Cookbook"}
                """;

        assertThatThrownBy(() -> mockMvc.perform(post("/api/certificates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)))
                .hasMessageContaining("font exploded");

        verify(analyticsService).trackCertificateError(contains("font exploded"), any());
        verify(storageService, never()).storeCertificate(any(), any());
    }
    
    @Test
    void shouldRejectInvalidBookTitle() throws Exception {
        // Given
        String requestJson = """
                {
                    "purchaserName": "Test User",
                    "bookTitle": "Invalid Book Title",
                    "purchaserEmail": "test@example.com"
                }
                """;

        // When/Then
        mockMvc.perform(post("/api/certificates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors.bookTitle").exists());
    }
    
    @Test
    void shouldReturnAvailableBooks() throws Exception {
        mockMvc.perform(get("/api/certificates/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableBooks").isArray())
                .andExpect(jsonPath("$.availableBooks.length()").value(6));
    }
    
    @Test
    void shouldListStoredCertificates() throws Exception {
        // Given
        List<Path> mockCertificates = List.of(
            Paths.get("/test/cert1.pdf"),
            Paths.get("/test/cert2.pdf")
        );
        when(storageService.listAllCertificates()).thenReturn(mockCertificates);
        when(storageService.getStoragePath()).thenReturn(Paths.get("/test"));
        
        // When/Then
        mockMvc.perform(get("/api/certificates/stored"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.certificates").isArray())
                .andExpect(jsonPath("$.count").value(2));
    }
    
    @Test
    void shouldGetStoredCertificate() throws Exception {
        // Given - create a temporary real file for testing
        Path tempPdf = Files.createTempFile("test-cert-", ".pdf");
        try {
            // Create a simple PDF file
            Files.writeString(tempPdf, "Test PDF content");
            
            // Set up the mock to return the real file
            String filename = tempPdf.getFileName().toString();
            when(storageService.getCertificate(filename)).thenReturn(tempPdf);
            
            // When/Then - should work with a real file
            mockMvc.perform(get("/api/certificates/stored/" + filename))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_PDF));
        } finally {
            // Clean up
            Files.deleteIfExists(tempPdf);
        }
    }
    @Test
    void shouldTrackDownloadWhenMetadataExists() throws Exception {
        Path tempPdf = Files.createTempFile("test-cert-", ".pdf");
        try {
            String filename = tempPdf.getFileName().toString();
            CertificateMetadata metadata = mock(CertificateMetadata.class);
            when(metadata.getCertificateId()).thenReturn("cert-42");
            when(storageService.getCertificate(filename)).thenReturn(tempPdf);
            when(metadataService.getCertificateMetadataByFilename(filename)).thenReturn(metadata);

            mockMvc.perform(get("/api/certificates/stored/" + filename))
                    .andExpect(status().isOk());

            verify(analyticsService).trackCertificateDownloaded(eq("cert-42"), any());
        } finally {
            Files.deleteIfExists(tempPdf);
        }
    }

    @Test
    void shouldReturnNotFoundForUnknownStoredCertificate() throws Exception {
        when(storageService.getCertificate("missing.pdf"))
                .thenThrow(new IOException("Certificate not found: missing.pdf"));

        mockMvc.perform(get("/api/certificates/stored/missing.pdf"))
                .andExpect(status().isNotFound());

        verify(analyticsService, never()).trackCertificateDownloaded(any(), any());
    }

    @Test
    void shouldReturnServerErrorWhenStorageCannotBeListed() throws Exception {
        when(storageService.listAllCertificates()).thenThrow(new IOException("disk unavailable"));

        mockMvc.perform(get("/api/certificates/stored"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldDescribeSelfSignedSignature() throws Exception {
        mockMvc.perform(get("/api/certificates/signature-info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.signatureAlgorithm").value("SHA512withRSA"))
                .andExpect(jsonPath("$.keySize").value("4096 bits"));
    }
}

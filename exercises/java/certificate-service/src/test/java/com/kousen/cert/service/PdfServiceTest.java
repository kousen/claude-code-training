package com.kousen.cert.service;

import com.kousen.cert.config.ServerUrlConfig;
import com.kousen.cert.model.CertificateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the PdfService class that actually creates files
 */
class PdfServiceTest {

    @TempDir
    Path tempDir;
    
    private QrCodeGenerator qrCodeGenerator;
    private PdfService pdfService;
    
    @BeforeEach
    void setUp() {
        // Create a mock ServerUrlConfig
        ServerUrlConfig mockConfig = mock(ServerUrlConfig.class);
        when(mockConfig.getUrl()).thenReturn("https://test-server.com");
        
        // Create a real QR code generator with the mock config
        qrCodeGenerator = new QrCodeGenerator(mockConfig);
        
        // Create the service instance
        pdfService = new PdfService(qrCodeGenerator);
    }
    
    @Test
    void shouldCreateValidPdf() throws Exception {
        // Given
        CertificateRequest request = new CertificateRequest(
                "Test User",
                "Modern Java Recipes", // Use a valid book title from the allowed list
                Optional.of("test@example.com")
        );
        
        // When
        Path pdfPath = pdfService.createPdf(request);
        
        try {
            // Then
            assertThat(pdfPath).exists();
            assertThat(Files.size(pdfPath)).isGreaterThan(0);
            
            // Verify it's a PDF
            byte[] pdfData = Files.readAllBytes(pdfPath);
            assertThat(pdfData.length).isGreaterThan(100);
            
            // Check for PDF header signature
            byte[] pdfHeaderSignature = "%PDF".getBytes();
            byte[] firstFive = new byte[4];
            System.arraycopy(pdfData, 0, firstFive, 0, 4);
            assertThat(firstFive).isEqualTo(pdfHeaderSignature);
        } finally {
            // Clean up
            Files.deleteIfExists(pdfPath);
        }
    }
}
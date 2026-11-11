package com.kousen.cert.service;

import com.kousen.cert.model.CertificateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CertificateStorageServiceTest {

    @TempDir
    Path tempDir;
    
    private CertificateStorageService storageService;
    
    @BeforeEach
    void setUp() {
        storageService = new CertificateStorageService(tempDir.toString());
    }
    
    @Test
    void shouldStoreAndRetrieveCertificate() throws IOException {
        // Given
        CertificateRequest request = new CertificateRequest(
                "John Doe",
                "Modern Java Recipes",
                Optional.of("john@example.com")
        );
        
        // Create a fake PDF file
        Path tempPdf = Files.createTempFile(tempDir, "temp-cert-", ".pdf");
        Files.writeString(tempPdf, "Test PDF content");
        
        // When
        Path storedPath = storageService.storeCertificate(tempPdf, request);
        
        // Then
        assertThat(storedPath).exists();
        assertThat(storedPath.getFileName().toString()).contains("johndoe");
        assertThat(storedPath.getFileName().toString()).contains("modernjavarecipes");
        
        // Verify we can list certificates
        var certificates = storageService.listAllCertificates();
        assertThat(certificates).isNotEmpty();
        
        // At least one certificate should have the same filename
        boolean foundMatch = certificates.stream()
            .anyMatch(path -> path.getFileName().equals(storedPath.getFileName()));
        assertThat(foundMatch).isTrue();
        
        // Verify we can retrieve by filename
        Path retrievedPath = storageService.getCertificate(storedPath.getFileName().toString());
        assertThat(retrievedPath).isEqualTo(storedPath);
    }
    
    @Test
    void shouldSanitizeFilenames() throws IOException {
        // Given
        CertificateRequest request = new CertificateRequest(
                "John O'Doe & Family!",
                "Making Java Groovy",
                Optional.empty()
        );
        
        // Create a fake PDF file
        Path tempPdf = Files.createTempFile(tempDir, "temp-cert-", ".pdf");
        Files.writeString(tempPdf, "Test PDF content");
        
        // When
        Path storedPath = storageService.storeCertificate(tempPdf, request);
        
        // Then - special characters should be sanitized
        String filename = storedPath.getFileName().toString();
        assertThat(filename).contains("johnodoefamily");
        assertThat(filename).doesNotContain("&");
        assertThat(filename).doesNotContain("'");
        assertThat(filename).doesNotContain("!");
    }
    
    @Test
    void shouldAbbreviateLongBookTitles() throws IOException {
        // Given
        CertificateRequest request = new CertificateRequest(
                "Jane Smith",
                "Mockito Made Clear",
                Optional.empty()
        );
        
        // Create a fake PDF file
        Path tempPdf = Files.createTempFile(tempDir, "temp-cert-", ".pdf");
        Files.writeString(tempPdf, "Test PDF content");
        
        // When
        Path storedPath = storageService.storeCertificate(tempPdf, request);
        
        // Then - book title should be abbreviated if needed
        String filename = storedPath.getFileName().toString();
        assertThat(filename).contains("janesmith");
        assertThat(filename).contains("mockitomadeclear");
    }

    @Test
    void shouldFailWhenRequestedCertificateDoesNotExist() {
        assertThatThrownBy(() -> storageService.getCertificate("missing.pdf"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("missing.pdf");
    }
}

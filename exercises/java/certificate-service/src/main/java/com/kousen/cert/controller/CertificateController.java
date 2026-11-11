package com.kousen.cert.controller;

import com.kousen.cert.analytics.service.AnalyticsService;
import com.kousen.cert.analytics.service.CertificateMetadataService;
import com.kousen.cert.model.CertificateRequest;
import com.kousen.cert.service.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/certificates")
public class CertificateController {
    private static final Logger logger = LoggerFactory.getLogger(CertificateController.class);

    private final PdfService pdfService;
    private final PdfSigner pdfSigner;
    private final CertificateStorageService storageService;
    private final AnalyticsService analyticsService;
    private final CertificateMetadataService metadataService;

    public CertificateController(
            PdfService pdfService,
            PdfSigner pdfSigner,
            CertificateStorageService storageService,
            AnalyticsService analyticsService,
            CertificateMetadataService metadataService) {
        this.pdfService = pdfService;
        this.pdfSigner = pdfSigner;
        this.storageService = storageService;
        this.analyticsService = analyticsService;
        this.metadataService = metadataService;
    }

    @PostMapping(produces = "application/pdf")
    public ResponseEntity<FileSystemResource> create(@Valid @RequestBody CertificateRequest req, HttpServletRequest request) throws Exception {
        long startTime = System.currentTimeMillis();
        String certificateId = UUID.randomUUID().toString();
        
        try {
            // Generate the certificate
            Path unsigned = pdfService.createPdf(req);
            Path signed = pdfSigner.sign(unsigned);
            
            try {
                // Store a copy of the certificate
                Path storedCertificate = storageService.storeCertificate(signed, req);
                logger.info("Certificate stored successfully at: {}", storedCertificate);
                
                // Track analytics
                long duration = System.currentTimeMillis() - startTime;
                analyticsService.trackCertificateGenerated(
                    certificateId,
                    req.purchaserName(),
                    req.purchaserEmail().orElse(null),
                    req.bookTitle(),
                    duration,
                    request
                );
                
                // Save metadata
                metadataService.saveCertificateMetadata(certificateId, storedCertificate);
                
                // Return the certificate in the response
                FileSystemResource res = new FileSystemResource(signed);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"certificate.pdf\"")
                        .header("X-Certificate-Status",
                                "Self-signed - May show warnings in PDF readers")
                        .header("X-Certificate-Id", certificateId)
                        .body(res);
            } finally {
                // Clean up the temporary unsigned PDF if it still exists
                if (Files.exists(unsigned)) {
                    try {
                        Files.deleteIfExists(unsigned);
                    } catch (Exception e) {
                        logger.warn("Failed to delete temporary unsigned PDF: {}", unsigned, e);
                    }
                }
            }
        } catch (Exception e) {
            // Track error
            analyticsService.trackCertificateError(e.getMessage(), request);
            throw e;
        }
    }
    
    @GetMapping("/books")
    public ResponseEntity<Map<String, Object>> getAvailableBooks() {
        Map<String, Object> response = new HashMap<>();
        response.put("availableBooks", CertificateRequest.ALLOWED_BOOK_TITLES);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Lists all stored certificates.
     * 
     * @return List of certificate filenames and details
     */
    @GetMapping("/stored")
    public ResponseEntity<Map<String, Object>> listStoredCertificates() {
        try {
            List<Path> certificates = storageService.listAllCertificates();
            List<Map<String, Object>> certificateDetails = certificates.stream()
                    .map(path -> {
                        Map<String, Object> details = new HashMap<>();
                        details.put("filename", path.getFileName().toString());
                        // Add file metadata if file exists
                        if (Files.exists(path)) {
                            try {
                                details.put("size", Files.size(path));
                            } catch (IOException e) {
                                logger.warn("Error getting size for {}", path, e);
                            }
                            try {
                                details.put("lastModified", Files.getLastModifiedTime(path).toString());
                            } catch (IOException e) {
                                logger.warn("Error getting lastModified for {}", path, e);
                            }
                        }
                        return details;
                    })
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("storagePath", storageService.getStoragePath().toString());
            response.put("certificates", certificateDetails);
            response.put("count", certificateDetails.size());
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            logger.error("Error listing certificates", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error listing certificates", e);
        }
    }
    
    /**
     * Retrieves a stored certificate by filename.
     * 
     * @param filename The certificate filename
     * @return The certificate PDF file
     */
    @GetMapping("/stored/{filename:.+}")
    public ResponseEntity<FileSystemResource> getStoredCertificate(@PathVariable String filename, HttpServletRequest request) {
        try {
            Path certificatePath = storageService.getCertificate(filename);
            FileSystemResource resource = new FileSystemResource(certificatePath);
            
            // Track download if metadata exists
            var metadata = metadataService.getCertificateMetadataByFilename(filename);
            if (metadata != null) {
                analyticsService.trackCertificateDownloaded(metadata.getCertificateId(), request);
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .header("X-Certificate-Status",
                            "Self-signed - May show warnings in PDF readers")
                    .body(resource);
        } catch (IOException e) {
            logger.error("Certificate not found: {}", filename, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Certificate not found", e);
        }
    }
    
    @GetMapping("/signature-info")
    public ResponseEntity<Map<String, String>> getSignatureInfo() {
        return ResponseEntity.ok(Map.of(
            "certificateType", "Self-signed X.509",
            "signatureAlgorithm", "SHA512withRSA",
            "keySize", "4096 bits",
            "validationStatus", "This certificate is self-signed. Adobe and other PDF readers will display " +
                               "warnings because it's not from a trusted certificate authority (CA).",
            "userExperience", "Recipients will need to manually trust the certificate or simply " +
                              "accept the warning to view the certificate."
        ));
    }
    
}

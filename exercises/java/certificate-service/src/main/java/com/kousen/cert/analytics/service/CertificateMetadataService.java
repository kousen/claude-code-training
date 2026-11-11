package com.kousen.cert.analytics.service;

import com.kousen.cert.analytics.model.CertificateMetadata;
import com.kousen.cert.analytics.repository.CertificateMetadataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
public class CertificateMetadataService {
    private static final Logger logger = LoggerFactory.getLogger(CertificateMetadataService.class);
    
    private final CertificateMetadataRepository repository;
    
    public CertificateMetadataService(CertificateMetadataRepository repository) {
        this.repository = repository;
    }
    
    @Async
    public CompletableFuture<Void> saveCertificateMetadata(String certificateId, Path certificatePath) {
        return CompletableFuture.runAsync(() -> {
            try {
                CertificateMetadata metadata = new CertificateMetadata(
                    certificateId,
                    certificatePath.getFileName().toString()
                );
                
                // Calculate file size
                if (Files.exists(certificatePath)) {
                    metadata.setFileSize(Files.size(certificatePath));
                    
                    // Calculate file hash
                    try {
                        byte[] fileBytes = Files.readAllBytes(certificatePath);
                        MessageDigest md = MessageDigest.getInstance("SHA-256");
                        byte[] hashBytes = md.digest(fileBytes);
                        StringBuilder sb = new StringBuilder();
                        for (byte b : hashBytes) {
                            sb.append(String.format("%02x", b));
                        }
                        metadata.setFileHash(sb.toString());
                    } catch (Exception e) {
                        logger.warn("Could not calculate file hash for {}", certificatePath, e);
                    }
                }
                
                repository.save(metadata);
                logger.info("Saved certificate metadata for {}", certificateId);
            } catch (Exception e) {
                logger.error("Error saving certificate metadata", e);
            }
        });
    }
    
    public CertificateMetadata getCertificateMetadataByFilename(String filename) {
        return repository.findByFilename(filename).orElse(null);
    }
}
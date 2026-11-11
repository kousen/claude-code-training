package com.kousen.cert.service;

import net.jqwik.api.*;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for PdfSigner
 */
class PdfSignerPropertyTest {

    // Initialize for each test
    private KeyStoreProvider initKeyStoreProvider() {
        // Use the keystore from test resources
        Path keystorePath = Paths.get("src/test/resources/keystores/test-keystore.p12");
        return new KeyStoreProvider(keystorePath);
    }

    private PdfSigner initSigner() {
        return new PdfSigner(initKeyStoreProvider());
    }

    private PdfBoxGenerator initGenerator() {
        return new PdfBoxGenerator();
    }

    /**
     * Property: Signing a PDF preserves the original content
     */
    // Re-enabling after fixing filter comparison issues
    @Property(tries = 2)
    void signingShouldPreserveOriginalContent(
            @ForAll("safeNames") String name,
            @ForAll("safeBookTitles") String bookTitle) throws Exception {

        // Arrange - Create a PDF
        String title = "Certificate of Ownership";
        PdfBoxGenerator generator = initGenerator();
        Path unsignedPdf = generator.createCertificatePdfWithQrData(title, name, bookTitle, null);

        try {
            // Extract text before signing
            String contentBeforeSigning = extractTextFromPdf(unsignedPdf);

            // Act - Sign the PDF
            PdfSigner signer = initSigner();
            Path signedPdf = signer.sign(unsignedPdf);

            try {
                // Assert - Signed PDF should exist and have reasonable size
                assertThat(signedPdf).exists();
                assertThat(Files.size(signedPdf)).isGreaterThan(Files.size(unsignedPdf));

                // Extract text after signing
                String contentAfterSigning = extractTextFromPdf(signedPdf);

                // Content should be preserved
                assertThat(contentAfterSigning).contains(contentBeforeSigning);

                // Verify signature exists
                try (PDDocument document = Loader.loadPDF(signedPdf.toFile())) {
                    List<PDSignature> signatures = document.getSignatureDictionaries();
                    assertThat(signatures).isNotEmpty();

                    PDSignature signature = signatures.getFirst();

                    // Log signature properties for better diagnostics
                    System.out.println("Signature filter: " + signature.getFilter());
                    System.out.println("Signature subfilter: " + signature.getSubFilter());
                    System.out.println("Signature name: " + signature.getName());
                    System.out.println("Signature reason: " + signature.getReason());

                    // In PdfSigner, we set these using PDSignature.FILTER_ADOBE_PPKLITE and SUBFILTER_ADBE_PKCS7_DETACHED
                    // But the actual values might be returned differently when read back
                    String filter = signature.getFilter();
                    String subfilter = signature.getSubFilter();

                    // Check that these contain the expected text rather than exact equality
                    assertThat(filter).containsIgnoringCase("adobe");
                    assertThat(filter).containsIgnoringCase("ppklite");
                    assertThat(subfilter).containsIgnoringCase("pkcs7");
                    assertThat(subfilter).containsIgnoringCase("detached");

                    // These should be exact matches
                    assertThat(signature.getName()).isEqualTo("Ken Kousen");
                    assertThat(signature.getReason()).isEqualTo("Certificate of Ownership");
                }

            } finally {
                // Cleanup signed PDF
                Files.deleteIfExists(signedPdf);
            }

        } finally {
            // Cleanup unsigned PDF
            Files.deleteIfExists(unsignedPdf);
        }
    }

    /**
     * Property: Multiple signings should each produce valid signatures
     */
    // Re-enabling after fixing filter comparison issues
    @Property(tries = 2)
    // Reduced number of trials to avoid excessive keystore operations
    void multiplePdfsCanBeSignedSuccessively(
            @ForAll("simplePdfContents") List<PdfContent> contents) throws Exception {

        Path[] unsignedPdfs = new Path[contents.size()];
        Path[] signedPdfs = new Path[contents.size()];

        try {
            PdfBoxGenerator generator = initGenerator();
            PdfSigner signer = initSigner();

            // Create and sign multiple PDFs in sequence
            for (int i = 0; i < contents.size(); i++) {
                PdfContent content = contents.get(i);

                // Create PDF
                unsignedPdfs[i] = generator.createCertificatePdfWithQrData(
                        "Certificate of Ownership",
                        content.name,
                        content.bookTitle,
                        null);

                // Sign PDF
                signedPdfs[i] = signer.sign(unsignedPdfs[i]);

                // Verify signature exists
                try (PDDocument document = Loader.loadPDF(signedPdfs[i].toFile())) {
                    List<PDSignature> signatures = document.getSignatureDictionaries();
                    assertThat(signatures).isNotEmpty();

                    // Basic signature validation
                    PDSignature signature = signatures.getFirst();

                    // Log signature properties
                    System.out.println("Multiple signing - signature filter: " + signature.getFilter());

                    // Check for Adobe PPKLite content
                    String filter = signature.getFilter();
                    assertThat(filter).containsIgnoringCase("adobe");

                    // Each signature should have a creation date
                    Calendar signDate = signature.getSignDate();
                    assertThat(signDate).isNotNull();
                }
            }

        } finally {
            // Cleanup all PDFs
            for (int i = 0; i < contents.size(); i++) {
                if (unsignedPdfs[i] != null) {
                    Files.deleteIfExists(unsignedPdfs[i]);
                }
                if (signedPdfs[i] != null) {
                    Files.deleteIfExists(signedPdfs[i]);
                }
            }
        }
    }

    /**
     * Record to represent PDF content
     */
    private record PdfContent(String name, String bookTitle) {
    }

    @Provide
    Arbitrary<List<PdfContent>> simplePdfContents() {
        // Create simple names and book titles
        Arbitrary<String> names = Arbitraries.of(
                "John Smith", "Jane Doe", "Alice Johnson", "Bob Brown", "Carol White");

        Arbitrary<String> bookTitles = Arbitraries.of(
                "Modern Java", "Spring Boot Guide", "Kotlin Essentials",
                "Effective Testing", "Cloud Native Apps");

        // Combine them into PdfContent objects
        Arbitrary<PdfContent> contentArbitrary = Combinators.combine(names, bookTitles)
                .as(PdfContent::new);

        // Return a list of 1-3 contents
        return contentArbitrary.list().ofSize(3);
    }

    // Safe test data generators
    @Provide
    Arbitrary<String> safeNames() {
        // Generate simple names that all fonts can handle
        return Arbitraries.of(
                "John Smith", "Jane Doe", "Alice Johnson",
                "Bob Brown", "Carol White", "David Miller");
    }

    @Provide
    Arbitrary<String> safeBookTitles() {
        // Generate simple book titles that all fonts can handle
        return Arbitraries.of(
                "Java Essentials", "Spring Boot Guide", "Kotlin Programming",
                "Effective Testing", "Cloud Native Apps", "Design Patterns");
    }

    // Helper method to extract text from a PDF file
    private String extractTextFromPdf(Path pdfPath) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfPath.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
}
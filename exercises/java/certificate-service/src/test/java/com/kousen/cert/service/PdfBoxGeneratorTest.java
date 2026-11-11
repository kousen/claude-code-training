package com.kousen.cert.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PdfBoxGeneratorTest {

    private static final String TITLE = "Certificate of Ownership";
    private static final String QR_CAPTION = "Scan to verify certificate authenticity";

    private final PdfBoxGenerator generator = new PdfBoxGenerator();
    private Path pdfPath;

    @AfterEach
    void deleteGeneratedPdf() throws IOException {
        if (pdfPath != null) {
            Files.deleteIfExists(pdfPath);
        }
    }

    @Test
    void shouldCreateCertificateWithTextAndQrCode() throws Exception {
        byte[] qrCode = new ClassPathResource("images/test-qr.png").getContentAsByteArray();

        pdfPath = generator.createCertificatePdfWithQrData(TITLE, "John Doe", "Test Book Title", qrCode);

        try (PDDocument document = Loader.loadPDF(pdfPath.toFile())) {
            assertThat(document.getNumberOfPages()).isEqualTo(1);
            assertThat(new PDFTextStripper().getText(document))
                    .contains(TITLE, "John Doe", "Test Book Title",
                            "This certifies that", "is the proud owner of", QR_CAPTION);
        }
    }

    @Test
    void shouldOmitQrCaptionWhenQrCodeIsNull() throws Exception {
        pdfPath = generator.createCertificatePdfWithQrData(TITLE, "Jane Smith", "Another Test Book", null);

        assertThat(textOf(pdfPath)).contains("Jane Smith", "Another Test Book").doesNotContain(QR_CAPTION);
    }

    @Test
    void shouldOmitQrCaptionWhenQrCodeIsEmpty() throws Exception {
        pdfPath = generator.createCertificatePdfWithQrData(TITLE, "Jane Smith", "Another Test Book", new byte[0]);

        assertThat(textOf(pdfPath)).contains("Jane Smith").doesNotContain(QR_CAPTION);
    }

    @Test
    void shouldRejectQrCodeBytesThatAreNotAnImage() throws IOException {
        byte[] notAnImage = "not a png".getBytes();

        long tempFilesBefore = countTempCertificates();

        assertThatThrownBy(() ->
                generator.createCertificatePdfWithQrData(TITLE, "Jane Smith", "Another Test Book", notAnImage))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not supported");

        assertThat(countTempCertificates())
                .as("a failed generation must not leave its temp file behind")
                .isEqualTo(tempFilesBefore);
    }

    private long countTempCertificates() throws IOException {
        Path tempDir = Path.of(System.getProperty("java.io.tmpdir"));
        try (var files = Files.list(tempDir)) {
            return files.filter(p -> p.getFileName().toString().matches("cert-.*\\.pdf")).count();
        }
    }

    private String textOf(Path pdf) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdf.toFile())) {
            assertThat(document.getNumberOfPages()).isEqualTo(1);
            return new PDFTextStripper().getText(document);
        }
    }
}

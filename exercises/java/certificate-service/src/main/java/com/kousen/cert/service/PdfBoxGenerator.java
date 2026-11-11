package com.kousen.cert.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * PDF generation utility using Apache PDFBox
 */
@Component
public class PdfBoxGenerator {

    // Font and color constants
    private static final Color GOLD_COLOR = new Color(255, 214, 92);
    
    // Font cache removed - PDType0Font must be loaded per document in PDFBox 3.0.4
    
    /**
     * Creates a certificate PDF using in-memory QR code data (avoids temporary image files).
     *
     * @param title        The main title text
     * @param name         The recipient's name
     * @param subtitle     The subtitle or book title
     * @param qrCodeData   QR code image bytes in PNG format
     * @return Path to the generated PDF file
     * @throws IOException If there's an error during PDF creation
     */
    public Path createCertificatePdfWithQrData(String title, String name, String subtitle, byte[] qrCodeData) throws IOException {
        Path pdfPath = Files.createTempFile("cert-", ".pdf");
        float pageWidth = PDRectangle.A4.getHeight();
        float pageHeight = PDRectangle.A4.getWidth();
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(new PDRectangle(pageWidth, pageHeight));
            document.addPage(page);

            PDFont titleFont = getFont(document, "CinzelDecorative-Regular.ttf",
                    new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD));
            PDFont nameFont = getFont(document, "GreatVibes-Regular.ttf",
                    new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE));
            PDFont textFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            addBackgroundImage(document, page);

            try (PDPageContentStream contentStream = new PDPageContentStream(
                    document, page, PDPageContentStream.AppendMode.APPEND, true)) {
                contentStream.setNonStrokingColor(GOLD_COLOR);

                float centerX = pageWidth / 2;
                float y = pageHeight - 200;
                drawCenteredText(contentStream, titleFont, 48, title, centerX, y);

                y -= 50;
                drawCenteredText(contentStream, textFont, 14, "This certifies that", centerX, y);

                y -= 60;
                drawCenteredText(contentStream, nameFont, 40, name, centerX, y);

                y -= 50;
                drawCenteredText(contentStream, textFont, 14, "is the proud owner of", centerX, y);

                y -= 40;
                drawCenteredText(contentStream, titleFont, 24, subtitle, centerX, y);

                y -= 50;
                drawCenteredText(contentStream, textFont, 14,
                        "and has earned the author's eternal gratitude.", centerX, y);

                if (qrCodeData != null && qrCodeData.length > 0) {
                    final float qrX = 80f;
                    final float qrY = 80f;
                    final float qrSize = 100f;
                    addQRCode(document, contentStream, qrCodeData, qrX, qrY);
                    // Center "Scan to verify certificate authenticity" below the QR code
                    drawCenteredText(contentStream, textFont, 8,
                            "Scan to verify certificate authenticity",
                            qrX + qrSize / 2f,
                            qrY - 20f);
                }
            }

            try {
                document.getDocumentCatalog().getAcroForm();
                document.setAllSecurityToBeRemoved(true);
                System.setProperty("org.apache.pdfbox.font.subset", "false");
                document.getDocumentInformation().setCustomMetadataValue("DisableFontSubsetting", "true");
                document.save(pdfPath.toFile());
                System.out.println("PDF created at: " + pdfPath.toAbsolutePath());
            } catch (Exception e) {
                System.err.println("Error saving PDF: " + e.getMessage());
                // Fallback to standard fonts
                try {
                    System.err.println("Attempting to save with standard fonts only");
                    try (PDDocument simpleDoc = new PDDocument()) {
                        PDPage simplePage = new PDPage(new PDRectangle(pageWidth, pageHeight));
                        simpleDoc.addPage(simplePage);
                        try (PDPageContentStream contentStream = new PDPageContentStream(
                                simpleDoc, simplePage, PDPageContentStream.AppendMode.APPEND, true)) {
                            PDFont stdFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                            PDFont stdBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                            PDFont stdItalic = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);
                            contentStream.setNonStrokingColor(GOLD_COLOR);
                            float y2 = pageHeight - 200;
                            drawCenteredText(contentStream, stdBold, 36, title, pageWidth / 2, y2);
                            y2 -= 50;
                            drawCenteredText(contentStream, stdFont, 14, "This certifies that", pageWidth / 2, y2);
                            y2 -= 60;
                            drawCenteredText(contentStream, stdItalic, 32, name, pageWidth / 2, y2);
                            y2 -= 50;
                            drawCenteredText(contentStream, stdFont, 14, "is the proud owner of", pageWidth / 2, y2);
                            y2 -= 40;
                            drawCenteredText(contentStream, stdBold, 22, subtitle, pageWidth / 2, y2);
                            y2 -= 50;
                            drawCenteredText(contentStream, stdFont, 14,
                                    "and has earned the author's eternal gratitude.", pageWidth / 2, y2);
                            if (qrCodeData != null && qrCodeData.length > 0) {
                                addQRCode(simpleDoc, contentStream, qrCodeData, 80, 80);
                                drawCenteredText(contentStream, stdFont, 8, "Scan to verify", 80, 60);
                            }
                        }
                        simpleDoc.save(pdfPath.toFile());
                    }
                    System.out.println("Successfully created simplified PDF with standard fonts only");
                } catch (Exception ex2) {
                    System.err.println("Error in fallback: " + ex2.getMessage());
                    throw new IOException("Failed to save PDF document: " + ex2.getMessage(), ex2);
                }
            }
        }
        return pdfPath;
    }
    /**
     * Adds an in-memory QR code to the certificate.
     */
    private void addQRCode(PDDocument document,
                           PDPageContentStream contentStream,
                           byte[] qrCodeData,
                           float x,
                           float y) throws IOException {
        // Create image object from in-memory QR bytes
        PDImageXObject qrImage = PDImageXObject.createFromByteArray(document, qrCodeData, "qrcode.png");
        // Draw QR code at fixed size (width and height in points) to control its display dimensions
        float qrSize = 100f;
        contentStream.drawImage(qrImage, x, y, qrSize, qrSize);
    }
    
    /**
     * Gets a font, loading from classpath resources
     */
    private PDFont getFont(PDDocument document, String fontFileName, PDFont fallbackFont) {
        // Note: In PDFBox 3.0.4, each font must be loaded directly into the document
        // Do not use the cache for PDType0Font as it causes issues on Heroku
        try {
            // Load the font from classpath resources
            var fontResource = new ClassPathResource("fonts/" + fontFileName);
            if (!fontResource.exists()) {
                System.err.println("Font file not found: " + fontFileName);
                return fallbackFont;
            }
            
            try (var fontStream = fontResource.getInputStream()) {
                // Set to false to disable subsetting which is causing issues on Heroku
                PDFont font = PDType0Font.load(document, fontStream, false);
                System.out.println("Successfully loaded font: " + fontFileName);
                return font;
            }
        } catch (IOException e) {
            System.err.println("Error loading font " + fontFileName + ": " + e.getMessage());
            System.err.println("Using fallback font: " + fallbackFont.getName());
            return fallbackFont;
        }
    }
    
    /**
     * Add background image to the PDF page
     */
    private void addBackgroundImage(PDDocument document, PDPage page) {
        try {
            var imageResource = new ClassPathResource("images/certificate-bg.png");
            try (var imageStream = imageResource.getInputStream()) {
                PDImageXObject backgroundImage = PDImageXObject.createFromByteArray(document, 
                        imageStream.readAllBytes(), "background");
                
                // Create a content stream to draw the background
                try (PDPageContentStream contentStream = new PDPageContentStream(document, page, 
                        PDPageContentStream.AppendMode.PREPEND, true)) {
                    // Draw the image at full page size
                    contentStream.drawImage(backgroundImage, 0, 0, page.getMediaBox().getWidth(), 
                            page.getMediaBox().getHeight());
                }
            }
        } catch (IOException e) {
            System.err.println("Error adding background image: " + e.getMessage());
            // Continue without background if image can't be loaded
        }
    }
    
    /**
     * Draw text centered at a specific position
     */
    private void drawCenteredText(PDPageContentStream contentStream, PDFont font, 
                                 float fontSize, String text, float centerX, float y) throws IOException {
        // Calculate text width to center it
        float textWidth = font.getStringWidth(text) / 1000 * fontSize;
        float startX = centerX - (textWidth / 2);
        
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(startX, y);
        contentStream.showText(text);
        contentStream.endText();
    }
}
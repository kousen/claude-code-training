package com.kousen.cert.service;

import com.kousen.cert.model.CertificateRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FontTesterTest {

    @Test
    void shouldGenerateOneCertificateForAnAllowedBook() throws Exception {
        PdfService pdfService = mock(PdfService.class);
        when(pdfService.createPdf(any())).thenReturn(Path.of("font-test.pdf"));

        new FontTester(pdfService).run();

        ArgumentCaptor<CertificateRequest> request = ArgumentCaptor.forClass(CertificateRequest.class);
        verify(pdfService).createPdf(request.capture());
        assertThat(CertificateRequest.ALLOWED_BOOK_TITLES).contains(request.getValue().bookTitle());
    }
}

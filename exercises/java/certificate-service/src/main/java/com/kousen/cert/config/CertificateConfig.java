package com.kousen.cert.config;

import com.kousen.cert.service.KeyStoreProvider;
import com.kousen.cert.service.PdfService;
import com.kousen.cert.service.PdfSigner;
import com.kousen.cert.service.QrCodeGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class CertificateConfig implements WebMvcConfigurer {

    @Bean
    PdfService pdfService(QrCodeGenerator qrCodeGenerator) { 
        return new PdfService(qrCodeGenerator); 
    }

    @Bean
    KeyStoreProvider keyStoreProvider(@Value("${certificate.keystore}") String keystorePath) {
        return new KeyStoreProvider(Paths.get(keystorePath));
    }

    @Bean
    PdfSigner pdfSigner(KeyStoreProvider provider) {
        return new PdfSigner(provider);
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Make fonts accessible
        registry.addResourceHandler("/fonts/**")
                .addResourceLocations("classpath:/fonts/");
        
        // Make images accessible
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/images/");
        
        // Make CSS files accessible
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
    }
}
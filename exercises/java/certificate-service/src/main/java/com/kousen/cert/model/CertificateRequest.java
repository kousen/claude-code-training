package com.kousen.cert.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Optional;

/**
 * Request object for certificate generation.
 * <p>
 * Note: The validation annotations include explicit message attributes which
 * improve the clarity of error messages for API users. While some IDE inspections
 * may flag these as redundant (since they match the default messages), they
 * provide value by making the validation requirements more explicit.
 * </p>
 */
public record CertificateRequest(
        @NotBlank(message = "Purchaser name is required") 
        String purchaserName,
        
        @NotBlank(message = "Book title is required")
        @ValidBookTitle(message = "Invalid book title")
        String bookTitle,
        
        Optional<@Email(message = "Invalid email format") String> purchaserEmail
) {
    // List of allowed book titles
    public static final List<String> ALLOWED_BOOK_TITLES = List.of(
            "Gradle Recipes for Android",
            "Help Your Boss Help You",
            "Kotlin Cookbook",
            "Making Java Groovy",
            "Mockito Made Clear",
            "Modern Java Recipes"
    );
}

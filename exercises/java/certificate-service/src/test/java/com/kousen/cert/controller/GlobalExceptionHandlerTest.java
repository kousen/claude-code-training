package com.kousen.cert.controller;

import com.kousen.cert.model.CertificateRequest;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @SuppressWarnings("unchecked")
    void shouldMapConstraintViolationsToFieldErrors() {
        ProblemDetail problem;
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            var violations = validator.validate(
                    new CertificateRequest("", "Not A Real Book", Optional.empty()));

            problem = handler.handleConstraintViolation(new ConstraintViolationException(violations));
        }

        assertThat(problem.getStatus()).isEqualTo(400);
        assertThat(problem.getTitle()).isEqualTo("Validation Error");
        assertThat(problem.getProperties()).containsKey("timestamp");
        assertThat((Map<String, String>) problem.getProperties().get("errors"))
                .containsEntry("purchaserName", "Purchaser name is required")
                .containsEntry("bookTitle", "Invalid book title");
    }

    @Test
    void shouldMapIllegalArgumentToBadRequest() {
        ProblemDetail problem = handler.handleIllegalArgumentException(
                new IllegalArgumentException("size must be positive"));

        assertThat(problem.getStatus()).isEqualTo(400);
        assertThat(problem.getTitle()).isEqualTo("Bad Request");
        assertThat(problem.getDetail()).isEqualTo("size must be positive");
        assertThat(problem.getProperties()).containsKey("timestamp");
    }
}

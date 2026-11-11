# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands
- Build: `./gradlew build`
- Run application: `./gradlew bootRun`
- Run all tests: `./gradlew test`
- Run single test: `./gradlew test --tests "com.kousen.cert.service.KeyStoreProviderTest"`
- Run property tests: `./gradlew test --tests "com.kousen.cert.service.QrCodeGeneratorPropertyTest"`
- Clean build: `./gradlew clean build`
- Coverage minimums (opt-in, not part of `check`): `./gradlew jacocoTestCoverageVerification`
- Coverage report: `build/jacocoHtml/index.html`

## Test Environment
- `AnalyticsIntegrationTest` needs Docker (Testcontainers PostgreSQL); it is skipped when Docker is unavailable
- `build.gradle.kts` overrides the Boot-managed Testcontainers version to 1.21.4 because older clients fail against Docker Engine 29; drop the override once Spring Boot manages 1.21.4 or later
- `@WebMvcTest` slices load `CertificateConfig`, so they need a `@MockitoBean PdfService`

## Test Resources
- When creating tests, ensure that necessary resources (fonts, images, etc.) are also available in src/test/resources
- Property-based tests should use controlled parameter generation for cryptographic tests to avoid ASN.1 parsing issues

## Code Style Guidelines
- Java 21 with Spring Boot 3.4.4
- Organize imports: java.*, javax.*, org.*, com.* (alphabetical within groups)
- Prefer Optional<T> over null for optional values
- Use explicit constructor injection for Spring dependencies
- Exception handling: specific exceptions with meaningful messages
- Test naming: descriptive method names with 'should' prefix
- Use static imports for JUnit and Mockito methods
- Maintain separation of concerns (controller/service/model)
- Use final for immutable fields
- Use records for simple data classes wherever possible

## Architecture Overview
- Full description with diagrams and known gaps: `ARCHITECTURE.md`. Update it when packages, request flow or the data model change.
- **Main Application**: Certificate generation with PDF signing and QR codes
- **Analytics Package**: Comprehensive tracking and dashboard system under `com.kousen.cert.analytics`
- **Database**: H2 for development, PostgreSQL for production via environment variables
- **Async Processing**: Event tracking runs in background tasks; read anything needed from the `HttpServletRequest` before going async, never inside the task
- **Web UI**: Thymeleaf templates with Bootstrap and Chart.js for analytics dashboard
- **No authentication**: there is no Spring Security dependency; `/admin/dashboard` and `/api/analytics/*` are open

## Runtime Configuration
- Keystore: `CERTIFICATE_KEYSTORE` (default `~/.cert_keystore.p12`), password `CERT_PWD` (default `changeit`); one `KeyStoreProvider` bean in `CertificateConfig`, injected wherever the key is needed
- Stored PDFs: `CERTIFICATE_STORAGE_PATH` (default `~/certificate-service/certificates`)
- QR code links: `CERTIFICATE_VERIFICATION_BASE_URL`, which defaults to `SERVER_URL` (the deployed Heroku host)
- A default local run writes the keystore and PDFs under the home directory

## Database Configuration
- Uses environment variable overrides: DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD (driver and dialect are auto-detected from the URL)
- Hibernate DDL mode configurable via HIBERNATE_DDL_AUTO in the default profile; the production profile hard-codes `update`
- Production profile available in application-production.yaml
- Analytics tables: certificate_events, certificate_metadata

## Analytics Features
- Real-time event tracking for certificate generation, verification, and downloads
- Dashboard at /admin/dashboard with charts and metrics
- REST API endpoints under /api/analytics/ for programmatic access
- Async event processing to avoid blocking main application flow

## Database Compatibility Notes
- SQL queries must work with both H2 and PostgreSQL
- Use CAST() functions instead of database-specific functions (e.g., CAST(timestamp AS DATE) not DATE())
- JPA @Query ORDER BY clauses should reference entity attributes, not SELECT aliases
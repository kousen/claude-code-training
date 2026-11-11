# Certificate Service

A Spring Boot application that generates personalized certificates of ownership for books, complete with digital signatures and QR code verification.

## Live Demo

A deployed version of this service is available at:
**https://certificate-service.kousenit.com**

You can try the certificate generation feature directly on the web interface.

## Overview

This service generates professionally designed certificates of ownership for book purchases. Each certificate:

- Is personalized with the purchaser's name
- Includes the book title
- Features an elegant background design
- Is digitally signed with a 4096-bit RSA key
- Includes a QR code for online verification

## Features

- **PDF Generation**: Draws each certificate directly with PDFBox: background image, decorative fonts and QR code
- **Digital Signatures**: Each PDF is cryptographically signed for authenticity
- **Certificate Verification**: QR codes link to a verification page with signature details
- **Certificate Storage**: Maintains copies of all generated certificates
- **Analytics Dashboard**: Comprehensive analytics with charts, metrics, and usage tracking
- **RESTful API**: Simple API for certificate generation and retrieval

## Technologies

- Java 21 (with virtual threads)
- Spring Boot 3.4
- Spring Data JPA with Hibernate
- H2 Database (development) / PostgreSQL (production)
- PDFBox 3.0 for PDF manipulation and signing
- BouncyCastle for cryptography
- ZXing for QR code generation
- Thymeleaf templating for web UI
- Chart.js for analytics visualizations
- jqwik for property-based testing

## Architecture

The application has two halves: a certificate core (`controller`, `service`, `model`, `config`) that validates, draws, signs and stores PDFs, and an `analytics` package that records events in a database and serves a dashboard.

See **[ARCHITECTURE.md](ARCHITECTURE.md)** for diagrams of the request flow, signing, verification, the analytics pipeline and the data model, plus a list of known gaps.

## API Endpoints

### Create a Certificate

```
POST /api/certificates
```

Request body:
```json
{
  "purchaserName": "Ada Lovelace",
  "bookTitle": "Modern Java Recipes",
  "purchaserEmail": "ada@example.com"
}
```

Response: the signed PDF (`Content-Type: application/pdf`, `Content-Disposition: inline`), with the generated id in an `X-Certificate-Id` header. Invalid input returns `400` with an RFC 7807 problem body listing the failing fields.

### Available Book Titles

```
GET /api/certificates/books
```

Returns the list of available book titles that can be used in certificate generation.

### List Stored Certificates

```
GET /api/certificates/stored
```

Returns a list of all stored certificates.

### Retrieve a Stored Certificate

```
GET /api/certificates/stored/{filename}
```

Returns a specific stored certificate by filename.

### Signature Information

```
GET /api/certificates/signature-info
```

Describes the signing certificate (self-signed X.509, SHA512withRSA, 4096-bit key) and why PDF readers warn about it.

### Certificate Verification

```
GET /verify-certificate
```

Displays the `name`, `book` and `date` query parameters from the QR code, the SHA-256 fingerprint of the signing certificate, and instructions for validating the digital signature.

### Analytics Dashboard

```
GET /admin/dashboard
```

Web interface showing comprehensive analytics including:
- Certificate generation metrics and trends
- Book popularity statistics
- Performance metrics and response times
- Recent activity logs

### Analytics API

```
GET /api/analytics/dashboard    # Complete dashboard data
GET /api/analytics/summary      # Summary statistics
GET /api/analytics/trends       # Daily trend data
GET /api/analytics/books        # Book popularity data
GET /api/analytics/performance  # Performance metrics
```

Returns JSON data for programmatic access to analytics.

## Running Locally

### Prerequisites

- Java 21 or higher
- Docker, only if you want the PostgreSQL integration test to run (it is skipped without Docker)

No Gradle installation is needed; use the included wrapper (`./gradlew`).

### Steps

1. Clone the course repository and go to this project
   ```bash
   git clone https://github.com/kousen/claude-code-training.git
   cd claude-code-training/exercises/java/certificate-service
   ```

2. Run the application
   ```bash
   ./gradlew bootRun
   ```

3. Generate a certificate
   ```bash
   curl -X POST http://localhost:8080/api/certificates \
        -H "Content-Type: application/json" \
        -d '{"purchaserName":"Ada Lovelace","bookTitle":"Modern Java Recipes"}' \
        -o ada.pdf
   open ada.pdf
   ```

4. Open the web form at `http://localhost:8080` or the analytics dashboard at `http://localhost:8080/admin/dashboard`. `requests.http` has ready-made requests for the other endpoints.

### What a local run touches

With the default settings the application:

- creates a keystore at `~/.cert_keystore.p12` on first start, if none exists
- saves a copy of every generated PDF under `~/certificate-service/certificates`
- keeps analytics in an in-memory H2 database, so that data is lost on restart (console at `/h2-console`)
- encodes the **deployed** site's address in QR codes, because `server.url` defaults to the Heroku host; set `CERTIFICATE_VERIFICATION_BASE_URL=http://localhost:8080` to make QR codes point at your machine

### Configuration

Every setting can be overridden with an environment variable.

| Environment variable | Default | Purpose |
|---|---|---|
| `CERTIFICATE_KEYSTORE` | `~/.cert_keystore.p12` | PKCS#12 keystore holding the signing key; created if missing |
| `CERT_PWD` | `changeit` | Keystore password (environment variable or system property) |
| `CERTIFICATE_STORAGE_PATH` | `~/certificate-service/certificates` | Directory for stored PDFs |
| `SERVER_URL` | the Heroku app URL | Public address of the service |
| `CERTIFICATE_VERIFICATION_BASE_URL` | value of `SERVER_URL` | Base URL encoded in QR codes |
| `DATABASE_URL` | `jdbc:h2:mem:testdb` | JDBC URL; a PostgreSQL URL switches the database |
| `DATABASE_USERNAME`, `DATABASE_PASSWORD` | `sa`, empty | Database credentials |
| `HIBERNATE_DDL_AUTO` | `update` | Schema handling in the default profile only (see Deployment) |

### Security

This service has **no authentication**. The analytics dashboard, the analytics API and the stored-certificate endpoints are open to anyone who can reach the server, and the H2 console is enabled outside the `production` profile. Stored filenames contain purchaser names. Put it behind your own access control before exposing it.

### Testing

```bash
./gradlew test                                   # all tests, with a JaCoCo report
./gradlew check                                  # tests plus the coverage thresholds
./gradlew test --tests "*IntegrationTest"        # end-to-end tests
./gradlew test --tests "com.kousen.cert.service.QrCodeGeneratorPropertyTest"
```

- The suite mixes example-based tests with property-based tests (jqwik), which try many generated inputs to find edge cases.
- `AnalyticsIntegrationTest` starts PostgreSQL in Docker through Testcontainers. It is skipped when Docker is not available.
- `./gradlew check` fails if coverage drops below the minimums set in `build.gradle.kts`. The HTML report is written to `build/jacocoHtml/index.html`.

## Deployment

### Heroku Deployment

The application is configured for easy Heroku deployment with PostgreSQL:

1. **Add PostgreSQL**: `heroku addons:create heroku-postgresql:essential-0`
2. **Set Environment Variables**:
   ```bash
   heroku config:set SPRING_PROFILES_ACTIVE=production
   ```
   The `production` profile sets `spring.jpa.hibernate.ddl-auto` to `update`, which creates the tables on first deployment. To switch to `validate` afterwards, set `SPRING_JPA_HIBERNATE_DDL_AUTO=validate`; `HIBERNATE_DDL_AUTO` has no effect under this profile.
3. **Deploy**: Standard git push to Heroku

The application automatically detects the Heroku PostgreSQL `DATABASE_URL` and switches from H2 to persistent storage.

Stored PDFs and a generated keystore live on the dyno's own disk, which does not survive a restart. Analytics in PostgreSQL persist; the files do not.

## Notes on Digital Signatures

The certificates are signed with a self-signed certificate, which means that Adobe Reader and other PDF readers may display warnings about the signature's validity. This is normal and doesn't affect the integrity of the signature itself. The verification page accessible via the QR code explains how to interpret these warnings.

## Font and Image Assets

The certificate uses these assets under `src/main/resources`; replace them to change its look:

```
fonts/CinzelDecorative-Regular.ttf
fonts/GreatVibes-Regular.ttf
images/certificate-bg.png
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Author

Ken Kousen - Tales from the Jar Side
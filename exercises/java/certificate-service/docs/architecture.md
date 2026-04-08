# Certificate Service — Architecture Diagrams

## 1. High-Level Component Overview

This diagram shows the major subsystems and how external actors interact with them.

```mermaid
%%{init: {'theme': 'dark'}}%%
flowchart TD
    subgraph Clients
        Browser["Browser / cURL"]
    end

    subgraph Web Layer
        HC["HomeController - GET /"]
        CC["CertificateController - /api/certificates"]
        VC["VerificationController - /verify-certificate"]
        ADC["AnalyticsDashboardController - /admin/dashboard"]
        AC["AnalyticsController - /api/analytics"]
        GEH["GlobalExceptionHandler"]
    end

    subgraph Certificate Engine
        PS["PdfService"]
        QR["QrCodeGenerator"]
        PBG["PdfBoxGenerator"]
        PSig["PdfSigner"]
        KSP["KeyStoreProvider"]
        CSVC["CertificateStorageService"]
    end

    subgraph Analytics Subsystem
        AS["AnalyticsService"]
        AES["AnalyticsEventService"]
        CMS["CertificateMetadataService"]
        MAS["MetricsAggregationService"]
        ATI["ApiTrackingInterceptor"]
    end

    subgraph Persistence
        H2["H2 Database - dev profile"]
        PG["PostgreSQL - production profile"]
    end

    subgraph External Libraries
        PDFBox["Apache PDFBox 3.0"]
        BC["BouncyCastle"]
        ZXing["ZXing"]
        Micrometer["Micrometer"]
    end

    Browser --> HC
    Browser --> CC
    Browser --> VC
    Browser --> ADC
    Browser --> AC

    CC --> PS
    CC --> PSig
    CC --> CSVC
    CC --> AS
    CC --> CMS

    VC --> AS
    VC --> KSP

    PS --> QR
    PS --> PBG
    PSig --> KSP

    QR --> ZXing
    PBG --> PDFBox
    PSig --> BC
    PSig --> PDFBox

    ADC --> AS
    AC --> AS
    AC --> MAS
    ATI -.->|intercepts requests| AS

    AS --> Micrometer
    AS --> H2
    AS --> PG
    CMS --> H2
    CMS --> PG

    classDef client fill:#546e7a,stroke:#90a4ae,color:#ffffff
    classDef web fill:#1565c0,stroke:#42a5f5,color:#ffffff
    classDef engine fill:#2e7d32,stroke:#66bb6a,color:#ffffff
    classDef analytics fill:#6a1b9a,stroke:#ab47bc,color:#ffffff
    classDef persistence fill:#e65100,stroke:#ff9800,color:#ffffff
    classDef extlib fill:#00695c,stroke:#26a69a,color:#ffffff

    class Browser client
    class HC,CC,VC,ADC,AC,GEH web
    class PS,QR,PBG,PSig,KSP,CSVC engine
    class AS,AES,CMS,MAS,ATI analytics
    class H2,PG persistence
    class PDFBox,BC,ZXing,Micrometer extlib
```

## 2. Class Dependency Diagram

Shows Spring bean wiring and key relationships between classes.

```mermaid
%%{init: {'theme': 'dark'}}%%
classDiagram
    class CertificateRequest {
        +String purchaserName
        +String bookTitle
        +Optional purchaserEmail
        +List ALLOWED_BOOK_TITLES
    }

    class CertificateController {
        -PdfService pdfService
        -PdfSigner pdfSigner
        -CertificateStorageService storageService
        -AnalyticsService analyticsService
        -CertificateMetadataService metadataService
        +create() ResponseEntity
        +getAvailableBooks() ResponseEntity
        +listStoredCertificates() ResponseEntity
        +getStoredCertificate() ResponseEntity
    }

    class PdfService {
        -QrCodeGenerator qrCodeGenerator
        -PdfBoxGenerator pdfGenerator
        +createPdf() Path
    }

    class QrCodeGenerator {
        -ServerUrlConfig serverConfig
        +generateQrCode() Path
        +generateQrCodeData() byte array
        -buildVerificationUrl() String
    }

    class PdfBoxGenerator {
        +createCertificatePdf() Path
        +createCertificatePdfWithQrData() Path
        -getFont() PDFont
        -addBackgroundImage() void
        -drawCenteredText() void
    }

    class PdfSigner {
        -KeyStoreProvider provider
        -PrivateKey privateKey
        -Certificate array certificateChain
        +sign Path to Path
        +sign InputStream to byte array
    }

    class KeyStoreProvider {
        -Path keystorePath
        +keyStore() KeyStore
    }

    class CertificateStorageService {
        -Path storagePath
        +storeCertificate() Path
        +listAllCertificates() List
        +getCertificate() Path
    }

    class VerificationController {
        -String certificateFingerprint
        -AnalyticsService analyticsService
        +verifyPage() String
    }

    class AnalyticsService {
        -CertificateEventRepository eventRepository
        -CertificateMetadataRepository metadataRepository
        -MeterRegistry meterRegistry
        +trackCertificateGenerated() CompletableFuture
        +trackCertificateDownloaded() CompletableFuture
        +trackCertificateVerified() CompletableFuture
        +trackCertificateError() CompletableFuture
        +getDashboardData() DashboardData
    }

    class CertificateEvent {
        -Long id
        -EventType eventType
        -String certificateId
        -String purchaserName
        -String bookTitle
        -Instant timestamp
        -Long durationMs
        -String ipAddress
    }

    class EventType {
        GENERATED
        VIEWED
        VERIFIED
        DOWNLOADED
        FAILED
        API_CALL
    }

    CertificateController --> PdfService
    CertificateController --> PdfSigner
    CertificateController --> CertificateStorageService
    CertificateController --> AnalyticsService
    CertificateController ..> CertificateRequest : validates

    PdfService --> QrCodeGenerator
    PdfService --> PdfBoxGenerator
    PdfSigner --> KeyStoreProvider

    VerificationController --> AnalyticsService
    VerificationController --> KeyStoreProvider

    AnalyticsService --> CertificateEvent : persists

    CertificateEvent --> EventType

    style CertificateController fill:#1565c0,stroke:#42a5f5,color:#ffffff
    style VerificationController fill:#1565c0,stroke:#42a5f5,color:#ffffff
    style PdfService fill:#2e7d32,stroke:#66bb6a,color:#ffffff
    style QrCodeGenerator fill:#2e7d32,stroke:#66bb6a,color:#ffffff
    style PdfBoxGenerator fill:#2e7d32,stroke:#66bb6a,color:#ffffff
    style PdfSigner fill:#2e7d32,stroke:#66bb6a,color:#ffffff
    style KeyStoreProvider fill:#2e7d32,stroke:#66bb6a,color:#ffffff
    style CertificateStorageService fill:#2e7d32,stroke:#66bb6a,color:#ffffff
    style AnalyticsService fill:#6a1b9a,stroke:#ab47bc,color:#ffffff
    style CertificateEvent fill:#e65100,stroke:#ff9800,color:#ffffff
    style EventType fill:#e65100,stroke:#ff9800,color:#ffffff
    style CertificateRequest fill:#00695c,stroke:#26a69a,color:#ffffff
```

## 3. Certificate Generation Sequence

The main flow when `POST /api/certificates` is called.

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'actorBkg': '#1565c0', 'actorTextColor': '#ffffff', 'actorBorder': '#42a5f5', 'noteBkgColor': '#2e7d32', 'noteTextColor': '#ffffff', 'noteBorderColor': '#66bb6a', 'activationBkgColor': '#37474f', 'activationBorderColor': '#78909c' }}}%%
sequenceDiagram
    actor Client
    participant CC as CertificateController
    participant PS as PdfService
    participant QR as QrCodeGenerator
    participant PBG as PdfBoxGenerator
    participant PSig as PdfSigner
    participant KSP as KeyStoreProvider
    participant CSVC as CertificateStorageService
    participant AS as AnalyticsService
    participant CMS as CertificateMetadataService
    participant DB as Database

    Client->>CC: POST /api/certificates
    Note over CC: Validate CertificateRequest,<br/>generate UUID, start timer

    CC->>PS: createPdf(request)
    PS->>QR: generateQrCodeData(name, bookTitle, 220)
    Note over QR: Build verification URL<br/>using ServerUrlConfig
    QR-->>PS: PNG byte array

    PS->>PBG: createCertificatePdfWithQrData(...)
    Note over PBG: Landscape A4 PDF,<br/>custom fonts with fallback,<br/>background image, QR embed
    PBG-->>PS: Path to unsigned PDF
    PS-->>CC: Path to unsigned PDF

    CC->>PSig: sign(unsignedPath)
    PSig->>KSP: keyStore()
    KSP-->>PSig: KeyStore
    Note over PSig: PDSignature PKCS7 detached,<br/>SHA512withRSA via BouncyCastle,<br/>save incrementally
    PSig-->>CC: Path to signed PDF

    CC->>CSVC: storeCertificate(signedPath, request)
    Note over CSVC: Sanitize filename,<br/>copy to storage dir
    CSVC-->>CC: Path to stored copy

    CC-)AS: trackCertificateGenerated - async
    AS-)DB: save CertificateEvent GENERATED
    Note over AS: Update Micrometer counters

    CC->>CMS: saveCertificateMetadata(id, path)
    CMS->>DB: save CertificateMetadata

    Note over CC: Delete unsigned temp file

    CC-->>Client: 200 OK with PDF body
```

## 4. QR Code Verification Flow

What happens when someone scans the QR code on a certificate.

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'actorBkg': '#1565c0', 'actorTextColor': '#ffffff', 'actorBorder': '#42a5f5', 'noteBkgColor': '#2e7d32', 'noteTextColor': '#ffffff', 'noteBorderColor': '#66bb6a', 'activationBkgColor': '#37474f', 'activationBorderColor': '#78909c' }}}%%
sequenceDiagram
    actor User
    participant QR as QR Code Scanner
    participant VC as VerificationController
    participant AS as AnalyticsService
    participant DB as Database
    participant TL as Thymeleaf Template

    User->>QR: Scan QR code on certificate
    QR->>VC: GET /verify-certificate with query params

    Note over VC: Extract name, book,<br/>date, id from params

    alt certificateId present
        VC-)AS: trackCertificateVerified - async
        AS-)DB: save CertificateEvent VERIFIED
        AS-)DB: increment verification count
    end

    VC->>TL: render verify-certificate template
    Note over TL: Display name, book title,<br/>issue date, fingerprint

    TL-->>User: HTML verification page
```

## 5. Analytics Data Flow

How analytics events flow from request interception through to the dashboard.

```mermaid
%%{init: {'theme': 'dark'}}%%
flowchart LR
    subgraph Event Sources
        CC["CertificateController<br/>GENERATED / FAILED"]
        VC["VerificationController<br/>VERIFIED"]
        DL["Download Endpoint<br/>DOWNLOADED"]
        ATI["ApiTrackingInterceptor<br/>API_CALL"]
    end

    subgraph Async Processing
        AS["AnalyticsService<br/>async methods"]
    end

    subgraph Persistence
        ER["CertificateEventRepository"]
        MR["CertificateMetadataRepository"]
        DB[("H2 / PostgreSQL")]
    end

    subgraph Metrics
        MIC["Micrometer MeterRegistry"]
        ACT["Spring Actuator"]
    end

    subgraph Dashboard
        ADC["AnalyticsDashboardController"]
        ARC["AnalyticsController"]
        MAS["MetricsAggregationService"]
    end

    CC --> AS
    VC --> AS
    DL --> AS
    ATI --> AS

    AS --> ER --> DB
    AS --> MR --> DB
    AS --> MIC --> ACT

    DB --> ADC
    DB --> ARC
    DB --> MAS

    classDef source fill:#1565c0,stroke:#42a5f5,color:#ffffff
    classDef async fill:#6a1b9a,stroke:#ab47bc,color:#ffffff
    classDef persist fill:#e65100,stroke:#ff9800,color:#ffffff
    classDef metric fill:#00695c,stroke:#26a69a,color:#ffffff
    classDef dash fill:#1a237e,stroke:#5c6bc0,color:#ffffff

    class CC,VC,DL,ATI source
    class AS async
    class ER,MR,DB persist
    class MIC,ACT metric
    class ADC,ARC,MAS dash
```

## 6. Database Schema

```mermaid
%%{init: {'theme': 'dark'}}%%
erDiagram
    CERTIFICATE_EVENTS {
        bigint id PK
        varchar event_type
        varchar certificate_id
        varchar purchaser_name
        varchar purchaser_email
        varchar book_title
        timestamp timestamp
        bigint duration_ms
        varchar ip_address
        varchar user_agent
        text error_message
        varchar endpoint
    }

    CERTIFICATE_METADATA {
        varchar certificate_id PK
        varchar file_path
        bigint verification_count
        timestamp created_at
        timestamp updated_at
    }

    AGGREGATED_METRICS {
        bigint id PK
        varchar metric_name
        varchar dimension
        double value
        date metric_date
        timestamp computed_at
    }

    CERTIFICATE_EVENTS ||--o| CERTIFICATE_METADATA : "certificate_id"
```

## Color Legend

| Color | Subsystem |
|-------|-----------|
| ![#1565c0](https://placehold.co/15x15/1565c0/1565c0.png) Blue | Web / Controller layer |
| ![#2e7d32](https://placehold.co/15x15/2e7d32/2e7d32.png) Green | Certificate Engine services |
| ![#6a1b9a](https://placehold.co/15x15/6a1b9a/6a1b9a.png) Purple | Analytics subsystem |
| ![#e65100](https://placehold.co/15x15/e65100/e65100.png) Orange | Persistence / Entities |
| ![#00695c](https://placehold.co/15x15/00695c/00695c.png) Teal | External libraries / Model |
| ![#546e7a](https://placehold.co/15x15/546e7a/546e7a.png) Gray | Clients |
| ![#1a237e](https://placehold.co/15x15/1a237e/1a237e.png) Indigo | Dashboard views |

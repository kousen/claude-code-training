---
name: project-cert-service-e2e
description: Architecture and patterns for the certificate-service Playwright e2e suite
metadata:
  type: project
---

E2e suite lives in `e2e/` (Node, @playwright/test ^1.49.0; lock file installs 1.60.0).
Three spec files: home.spec.ts, verification.spec.ts, dashboard.spec.ts — 6 tests total, 18 across 3 browsers (Chromium/Firefox/WebKit).

Key design decisions:
- `workers: 1`, `fullyParallel: false` — single shared H2 database; tests run serially to avoid state collisions.
- `webServer` boots the app via `./gradlew bootRun --args='--spring.profiles.active=uitest'`; `reuseExistingServer: !process.env.CI`.
- Dashboard tests seed via real `POST /api/certificates` API (no repo access from Node), then poll with `toPass({ timeout: 15_000 })` because analytics writes are `@Async`.
- Unique marker per test run (`E2E-${project.name}-${Date.now()}`) avoids coupling to absolute row counts.
- `application-uitest.yaml`: in-memory H2 (`DB_CLOSE_DELAY=-1`, `create-drop`), keystore/storage redirected to `build/uitest/`.
- Home page is a static HTML file (`src/main/resources/static/index.html`), not a Thymeleaf template.
- Book titles are validated server-side against `CertificateRequest.ALLOWED_BOOK_TITLES`; tests use valid titles (Modern Java Recipes, Kotlin Cookbook).
- `Content-Disposition` for the PDF is `inline; filename="certificate.pdf"` — the download is triggered client-side via `a.download = 'certificate.pdf'` in the page JS, which is what makes Playwright's `waitForEvent('download')` work.

**Why:** Added as a teaching demonstration for O'Reilly Claude Code training course.
**How to apply:** When reviewing further changes to e2e/, check selector anchors against static/index.html (form page), templates/verify-certificate.html (verification page), and templates/analytics/dashboard.html.

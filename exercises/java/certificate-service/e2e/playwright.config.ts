import { defineConfig, devices } from '@playwright/test';

/**
 * Playwright config for the Certificate Service UI.
 *
 * `webServer` boots the Spring Boot app (uitest profile: in-memory H2,
 * throwaway keystore/storage) before the suite runs and shuts it down after.
 *
 * Tests run serially (`workers: 1`, `fullyParallel: false`) because the app
 * and its H2 database are shared mutable state — the dashboard tests seed
 * data through the real API. Tests use unique markers rather than exact
 * counts so they stay correct as that shared DB accumulates rows.
 */
export default defineConfig({
  testDir: './tests',
  fullyParallel: false,
  workers: 1,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  reporter: 'html',
  use: {
    baseURL: 'http://localhost:8080',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
    { name: 'firefox', use: { ...devices['Desktop Firefox'] } },
    { name: 'webkit', use: { ...devices['Desktop Safari'] } },
  ],
  webServer: {
    command: "./gradlew bootRun --args='--spring.profiles.active=uitest'",
    cwd: '..',
    url: 'http://localhost:8080',
    timeout: 120_000,
    reuseExistingServer: !process.env.CI,
  },
});

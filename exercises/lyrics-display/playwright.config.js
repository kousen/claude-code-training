import { defineConfig, devices } from "@playwright/test";

// The app is a set of static files (no build step), so Playwright serves the
// directory itself with http-server and points the browser at it.
export default defineConfig({
  testDir: "./e2e",
  fullyParallel: true,
  // Writes a browsable report to playwright-report/; `open: "never"` keeps it
  // from auto-launching a browser after every run.
  reporter: [["html", { open: "never" }]],
  use: {
    baseURL: "http://127.0.0.1:4173",
    trace: "on-first-retry",
  },
  projects: [
    { name: "chromium", use: { ...devices["Desktop Chrome"] } },
  ],
  webServer: {
    command: "npx http-server -p 4173 -c-1 --silent",
    url: "http://127.0.0.1:4173",
    reuseExistingServer: !process.env.CI,
  },
});

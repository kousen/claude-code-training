import { defineConfig } from '@playwright/test';

// SLOWMO=800 pnpm exec playwright test --headed   -> visible browser, 800 ms pause per step
export default defineConfig({
  testDir: 'e2e',
  webServer: { command: 'python3 -m http.server 8765', port: 8765, reuseExistingServer: true },
  use: {
    baseURL: 'http://localhost:8765',
    launchOptions: { slowMo: Number(process.env.SLOWMO) || 0 },
  },
});

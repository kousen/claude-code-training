import { test, expect } from '@playwright/test';

test.describe('Analytics dashboard', () => {
  test('renders the dashboard shell with summary cards and charts', async ({ page }) => {
    await page.goto('/admin/dashboard');

    await expect(page).toHaveTitle('Certificate Analytics Dashboard');
    await expect(page.locator('.metric-value')).toHaveCount(4);
    await expect(page.locator('#trendChart')).toHaveCount(1);
    await expect(page.locator('#bookChart')).toHaveCount(1);
  });

  test('reflects newly generated certificates in the activity feed', async ({
    page,
    request,
  }, testInfo) => {
    // The app's H2 database is shared across all browser projects and
    // accumulates rows, so we assert on unique markers instead of totals.
    const marker = `E2E-${testInfo.project.name}-${Date.now()}`;
    const seeds = [
      { purchaserName: `${marker} Ada`, bookTitle: 'Modern Java Recipes' },
      { purchaserName: `${marker} Grace`, bookTitle: 'Kotlin Cookbook' },
    ];

    for (const body of seeds) {
      const response = await request.post('/api/certificates', { data: body });
      expect(response.ok()).toBeTruthy();
    }

    // Analytics events are written with @Async, so the dashboard may lag the
    // POST responses — poll until the seeded rows appear in the feed.
    await expect(async () => {
      await page.goto('/admin/dashboard');
      await expect(
        page.locator('.activity-item', { hasText: `${marker} Ada` }),
      ).toHaveCount(1);
      await expect(
        page.locator('.activity-item', { hasText: `${marker} Grace` }),
      ).toHaveCount(1);
    }).toPass({ timeout: 15_000 });
  });
});

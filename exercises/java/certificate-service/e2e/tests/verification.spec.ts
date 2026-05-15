import { test, expect } from '@playwright/test';

test.describe('Verification page', () => {
  test('displays certificate details from query parameters', async ({ page }) => {
    await page.goto(
      '/verify-certificate?name=Ada+Lovelace&book=Modern+Java+Recipes&date=2026-05-14',
    );

    await expect(page.locator('#purchaser-name')).toHaveText('Ada Lovelace');
    await expect(page.locator('#book-title')).toHaveText('Modern Java Recipes');
    await expect(page.locator('#issue-date')).toHaveText('2026-05-14');
    await expect(page.locator('.valid-badge')).toHaveText('Valid');
  });

  test('falls back to "Not specified" when query parameters are absent', async ({ page }) => {
    await page.goto('/verify-certificate');

    await expect(page.locator('#purchaser-name')).toHaveText('Not specified');
    await expect(page.locator('#book-title')).toHaveText('Not specified');
    await expect(page.locator('#issue-date')).toHaveText('Not specified');
    // The "Valid" badge is static markup, rendered regardless of input.
    await expect(page.locator('.valid-badge')).toHaveText('Valid');
  });
});

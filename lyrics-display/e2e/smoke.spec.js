import { test, expect } from '@playwright/test';

test('loads lyrics, plays one tick on Space, and shows the second line', async ({ page }) => {
  await page.goto('/');
  await expect(page.locator('#counter')).toHaveText('1 / 14');
  await page.keyboard.press('Space');
  await expect(page.locator('#play')).toHaveText('❚❚ Pause');
  await expect(page.locator('#counter')).toHaveText('2 / 14', { timeout: 4000 });
});

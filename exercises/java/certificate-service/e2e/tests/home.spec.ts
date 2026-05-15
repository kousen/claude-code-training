import { test, expect } from '@playwright/test';
import { readFile } from 'node:fs/promises';

test.describe('Home page - certificate generation form', () => {
  test('generates and downloads a signed PDF', async ({ page }) => {
    await page.goto('/');

    await page.fill('#name', 'Ada Lovelace');
    await page.selectOption('#book', 'Modern Java Recipes');

    const downloadPromise = page.waitForEvent('download');
    await page.click('#demo-form button[type="submit"]');
    const download = await downloadPromise;

    expect(download.suggestedFilename()).toBe('certificate.pdf');

    // Every PDF starts with the "%PDF" magic number.
    const path = await download.path();
    const buffer = await readFile(path);
    expect(buffer.subarray(0, 4).toString()).toBe('%PDF');
  });

  test('shows a validation alert when the name field is empty', async ({ page }) => {
    await page.goto('/');

    // A registered dialog handler (not waitForEvent) lets the click action
    // complete: it dismisses the alert so the click promise can resolve.
    let dialogMessage: string | undefined;
    page.once('dialog', async (dialog) => {
      dialogMessage = dialog.message();
      await dialog.dismiss();
    });

    // Name left blank; the book <select> already has a default value.
    await page.click('#demo-form button[type="submit"]');

    expect(dialogMessage).toBe('Please fill in all fields');
  });
});

import { test, expect } from "@playwright/test";

// Shared login helper — runs before each test in this file
test.beforeEach(async ({ page }) => {
  await page.goto("/");
  await page.locator('[data-test="username"]').fill("standard_user");
  await page.locator('[data-test="password"]').fill("secret_sauce");
  await page.locator('[data-test="login-button"]').click();
  await expect(page).toHaveURL(/inventory\.html/);
});

test.describe("Product Inventory", () => {
  test("displays six products", async ({ page }) => {
    const items = page.locator('[data-test="inventory-item"]');
    await expect(items).toHaveCount(6);
  });

  test("each product has name, description, price, and image", async ({ page }) => {
    const firstItem = page.locator('[data-test="inventory-item"]').first();
    await expect(firstItem.locator('[data-test="inventory-item-name"]')).toBeVisible();
    await expect(firstItem.locator('[data-test="inventory-item-desc"]')).toBeVisible();
    await expect(firstItem.locator('[data-test="inventory-item-price"]')).toBeVisible();
    await expect(firstItem.locator("img")).toBeVisible();
  });

  test("prices are formatted correctly", async ({ page }) => {
    const prices = page.locator('[data-test="inventory-item-price"]');
    const allPrices = await prices.allTextContents();
    for (const price of allPrices) {
      expect(price).toMatch(/^\$\d+\.\d{2}$/);
    }
  });
});

test.describe("Product Sorting", () => {
  test("sorts by name A to Z (default)", async ({ page }) => {
    const names = await page
      .locator('[data-test="inventory-item-name"]')
      .allTextContents();
    const sorted = [...names].sort();
    expect(names).toEqual(sorted);
  });

  test("sorts by name Z to A", async ({ page }) => {
    await page.locator('[data-test="product-sort-container"]').selectOption("za");

    const names = await page
      .locator('[data-test="inventory-item-name"]')
      .allTextContents();
    const sorted = [...names].sort().reverse();
    expect(names).toEqual(sorted);
  });

  test("sorts by price low to high", async ({ page }) => {
    await page.locator('[data-test="product-sort-container"]').selectOption("lohi");

    const prices = await page
      .locator('[data-test="inventory-item-price"]')
      .allTextContents();
    const numericPrices = prices.map((p) => parseFloat(p.replace("$", "")));
    const sorted = [...numericPrices].sort((a, b) => a - b);
    expect(numericPrices).toEqual(sorted);
  });

  test("sorts by price high to low", async ({ page }) => {
    await page.locator('[data-test="product-sort-container"]').selectOption("hilo");

    const prices = await page
      .locator('[data-test="inventory-item-price"]')
      .allTextContents();
    const numericPrices = prices.map((p) => parseFloat(p.replace("$", "")));
    const sorted = [...numericPrices].sort((a, b) => b - a);
    expect(numericPrices).toEqual(sorted);
  });
});

test.describe("Product Detail Page", () => {
  test("navigates to product detail and back", async ({ page }) => {
    await page.locator('[data-test="inventory-item-name"]').first().click();

    await expect(page).toHaveURL(/inventory-item\.html/);
    await expect(page.locator('[data-test="inventory-item-name"]')).toBeVisible();
    await expect(page.locator('[data-test="back-to-products"]')).toBeVisible();

    await page.locator('[data-test="back-to-products"]').click();
    await expect(page).toHaveURL(/inventory\.html/);
  });
});

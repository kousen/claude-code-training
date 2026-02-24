import { test, expect } from "@playwright/test";

// Login and add an item before each test
test.beforeEach(async ({ page }) => {
  await page.goto("/");
  await page.locator('[data-test="username"]').fill("standard_user");
  await page.locator('[data-test="password"]').fill("secret_sauce");
  await page.locator('[data-test="login-button"]').click();

  // Add an item and go to cart
  await page.locator('[data-test="add-to-cart-sauce-labs-backpack"]').click();
  await page.locator('[data-test="shopping-cart-link"]').click();
  await page.locator('[data-test="checkout"]').click();
  await expect(page).toHaveURL(/checkout-step-one\.html/);
});

test.describe("Checkout Step One - Your Information", () => {
  test("displays checkout information form", async ({ page }) => {
    await expect(page.locator('[data-test="firstName"]')).toBeVisible();
    await expect(page.locator('[data-test="lastName"]')).toBeVisible();
    await expect(page.locator('[data-test="postalCode"]')).toBeVisible();
  });

  test("shows error when first name is missing", async ({ page }) => {
    await page.locator('[data-test="continue"]').click();

    await expect(page.locator('[data-test="error"]')).toContainText(
      "First Name is required"
    );
  });

  test("shows error when last name is missing", async ({ page }) => {
    await page.locator('[data-test="firstName"]').fill("John");
    await page.locator('[data-test="continue"]').click();

    await expect(page.locator('[data-test="error"]')).toContainText(
      "Last Name is required"
    );
  });

  test("shows error when postal code is missing", async ({ page }) => {
    await page.locator('[data-test="firstName"]').fill("John");
    await page.locator('[data-test="lastName"]').fill("Doe");
    await page.locator('[data-test="continue"]').click();

    await expect(page.locator('[data-test="error"]')).toContainText(
      "Postal Code is required"
    );
  });

  test("proceeds to step two with valid info", async ({ page }) => {
    await page.locator('[data-test="firstName"]').fill("John");
    await page.locator('[data-test="lastName"]').fill("Doe");
    await page.locator('[data-test="postalCode"]').fill("10001");
    await page.locator('[data-test="continue"]').click();

    await expect(page).toHaveURL(/checkout-step-two\.html/);
  });

  test("cancel returns to cart", async ({ page }) => {
    await page.locator('[data-test="cancel"]').click();
    await expect(page).toHaveURL(/cart\.html/);
  });
});

test.describe("Checkout Step Two - Overview", () => {
  test.beforeEach(async ({ page }) => {
    await page.locator('[data-test="firstName"]').fill("John");
    await page.locator('[data-test="lastName"]').fill("Doe");
    await page.locator('[data-test="postalCode"]').fill("10001");
    await page.locator('[data-test="continue"]').click();
    await expect(page).toHaveURL(/checkout-step-two\.html/);
  });

  test("shows order summary with item details", async ({ page }) => {
    await expect(page.locator('[data-test="inventory-item"]')).toHaveCount(1);
    await expect(
      page.locator('[data-test="inventory-item-name"]')
    ).toHaveText("Sauce Labs Backpack");
  });

  test("displays payment and shipping info", async ({ page }) => {
    await expect(page.locator('[data-test="payment-info-label"]')).toBeVisible();
    await expect(page.locator('[data-test="shipping-info-label"]')).toBeVisible();
  });

  test("shows subtotal, tax, and total", async ({ page }) => {
    await expect(page.locator('[data-test="subtotal-label"]')).toContainText("$29.99");
    await expect(page.locator('[data-test="tax-label"]')).toBeVisible();
    await expect(page.locator('[data-test="total-label"]')).toBeVisible();
  });

  test("completes the order successfully", async ({ page }) => {
    await page.locator('[data-test="finish"]').click();

    await expect(page).toHaveURL(/checkout-complete\.html/);
    await expect(page.locator('[data-test="complete-header"]')).toHaveText(
      "Thank you for your order!"
    );
  });

  test("can return home after completing order", async ({ page }) => {
    await page.locator('[data-test="finish"]').click();
    await page.locator('[data-test="back-to-products"]').click();

    await expect(page).toHaveURL(/inventory\.html/);
  });
});

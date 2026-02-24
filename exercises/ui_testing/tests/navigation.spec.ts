import { test, expect } from "@playwright/test";

test.beforeEach(async ({ page }) => {
  await page.goto("/");
  await page.locator('[data-test="username"]').fill("standard_user");
  await page.locator('[data-test="password"]').fill("secret_sauce");
  await page.locator('[data-test="login-button"]').click();
});

test.describe("Hamburger Menu", () => {
  test("opens and shows menu items", async ({ page }) => {
    await page.getByRole("button", { name: "Open Menu" }).click();

    await expect(page.locator('[data-test="inventory-sidebar-link"]')).toBeVisible();
    await expect(page.locator('[data-test="about-sidebar-link"]')).toBeVisible();
    await expect(page.locator('[data-test="logout-sidebar-link"]')).toBeVisible();
    await expect(page.locator('[data-test="reset-sidebar-link"]')).toBeVisible();
  });

  test("navigates to All Items", async ({ page }) => {
    // Go to cart first so we can test navigating back
    await page.locator('[data-test="shopping-cart-link"]').click();
    await expect(page).toHaveURL(/cart\.html/);

    await page.getByRole("button", { name: "Open Menu" }).click();
    await page.locator('[data-test="inventory-sidebar-link"]').click();

    await expect(page).toHaveURL(/inventory\.html/);
  });

  test("logs out successfully", async ({ page }) => {
    await page.getByRole("button", { name: "Open Menu" }).click();
    await page.locator('[data-test="logout-sidebar-link"]').click();

    await expect(page).toHaveURL(/saucedemo\.com\/?$/);
    await expect(page.locator('[data-test="login-button"]')).toBeVisible();
  });

  test("closes menu with X button", async ({ page }) => {
    await page.getByRole("button", { name: "Open Menu" }).click();
    await expect(page.locator('[data-test="logout-sidebar-link"]')).toBeVisible();

    await page.getByRole("button", { name: "Close Menu" }).click();
    await expect(page.locator('[data-test="logout-sidebar-link"]')).not.toBeVisible();
  });
});

test.describe("Page Protection", () => {
  test("redirects to login when accessing inventory without auth", async ({
    page,
  }) => {
    // Log out first
    await page.getByRole("button", { name: "Open Menu" }).click();
    await page.locator('[data-test="logout-sidebar-link"]').click();

    // Try to access inventory directly
    await page.goto("/inventory.html");
    await expect(page).toHaveURL(/saucedemo\.com/);
    await expect(page.locator('[data-test="error"]')).toContainText(
      "You can only access"
    );
  });
});

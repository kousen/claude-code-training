import { test, expect } from "@playwright/test";

test.describe("Login Page", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/");
  });

  test("displays the login form", async ({ page }) => {
    await expect(page.locator('[data-test="username"]')).toBeVisible();
    await expect(page.locator('[data-test="password"]')).toBeVisible();
    await expect(page.locator('[data-test="login-button"]')).toBeVisible();
  });

  test("shows accepted usernames on the page", async ({ page }) => {
    const loginCredentials = page.locator("#login_credentials");
    await expect(loginCredentials).toContainText("standard_user");
    await expect(loginCredentials).toContainText("locked_out_user");
  });

  test("logs in with valid credentials", async ({ page }) => {
    await page.locator('[data-test="username"]').fill("standard_user");
    await page.locator('[data-test="password"]').fill("secret_sauce");
    await page.locator('[data-test="login-button"]').click();

    await expect(page).toHaveURL(/inventory\.html/);
    await expect(page.locator('[data-test="title"]')).toHaveText("Products");
  });

  test("shows error for locked out user", async ({ page }) => {
    await page.locator('[data-test="username"]').fill("locked_out_user");
    await page.locator('[data-test="password"]').fill("secret_sauce");
    await page.locator('[data-test="login-button"]').click();

    const error = page.locator('[data-test="error"]');
    await expect(error).toBeVisible();
    await expect(error).toContainText("locked out");
  });

  test("shows error for invalid credentials", async ({ page }) => {
    await page.locator('[data-test="username"]').fill("invalid_user");
    await page.locator('[data-test="password"]').fill("wrong_password");
    await page.locator('[data-test="login-button"]').click();

    const error = page.locator('[data-test="error"]');
    await expect(error).toBeVisible();
    await expect(error).toContainText("do not match");
  });

  test("shows error when username is missing", async ({ page }) => {
    await page.locator('[data-test="password"]').fill("secret_sauce");
    await page.locator('[data-test="login-button"]').click();

    await expect(page.locator('[data-test="error"]')).toContainText("Username is required");
  });

  test("shows error when password is missing", async ({ page }) => {
    await page.locator('[data-test="username"]').fill("standard_user");
    await page.locator('[data-test="login-button"]').click();

    await expect(page.locator('[data-test="error"]')).toContainText("Password is required");
  });
});

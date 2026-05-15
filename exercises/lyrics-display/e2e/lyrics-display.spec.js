import { test, expect } from "@playwright/test";

// End-to-end coverage for the wiring in app.js — the parts the node:test
// unit suite (player.test.js) can't reach: DOM rendering, button clicks,
// keyboard shortcuts, the speed slider, and file upload.

test.beforeEach(async ({ page }) => {
  await page.goto("/");
});

test("loads the built-in song on first paint", async ({ page }) => {
  await expect(page.locator("#song-title")).not.toHaveText("Lyrics Display");
  await expect(page.locator("#line")).not.toHaveText("Press Play or Next to begin");
  await expect(page.locator("#progress")).toHaveText(/^1 \/ \d+$/);
});

test("Next and Previous walk through the lines and wrap", async ({ page }) => {
  const line = page.locator("#line");
  const progress = page.locator("#progress");
  const total = Number((await progress.textContent()).split("/")[1].trim());

  const firstLine = await line.textContent();
  await page.locator("#next-btn").click();
  await expect(progress).toHaveText(`2 / ${total}`);
  await expect(line).not.toHaveText(firstLine);

  // Previous from line 1 wraps to the last line.
  await page.locator("#prev-btn").click();
  await page.locator("#prev-btn").click();
  await expect(progress).toHaveText(`${total} / ${total}`);
});

test("Play toggles the button into its Pause state", async ({ page }) => {
  const playBtn = page.locator("#play-btn");
  await expect(playBtn).toHaveAttribute("aria-pressed", "false");

  await playBtn.click();
  await expect(playBtn).toHaveAttribute("aria-pressed", "true");
  await expect(playBtn).toContainText("Pause");

  await playBtn.click();
  await expect(playBtn).toHaveAttribute("aria-pressed", "false");
  await expect(playBtn).toContainText("Play");
});

test("keyboard shortcuts drive navigation and playback", async ({ page }) => {
  const progress = page.locator("#progress");
  const total = Number((await progress.textContent()).split("/")[1].trim());

  await page.keyboard.press("ArrowRight");
  await expect(progress).toHaveText(`2 / ${total}`);

  await page.keyboard.press("ArrowLeft");
  await expect(progress).toHaveText(`1 / ${total}`);

  await page.keyboard.press("Space");
  await expect(page.locator("#play-btn")).toHaveAttribute("aria-pressed", "true");
});

test("the speed slider updates the displayed duration label", async ({ page }) => {
  const slider = page.locator("#speed-input");
  await slider.fill("5000");
  await expect(page.locator("#speed-value")).toHaveText("5s");

  await slider.fill("500");
  await expect(page.locator("#speed-value")).toHaveText("0.5s");
});

test("uploading a .txt file swaps in the new song", async ({ page }) => {
  await page.locator("#file-input").setInputFiles({
    name: "my-poem.txt",
    mimeType: "text/plain",
    buffer: Buffer.from("alpha\nbeta\ngamma\n"),
  });

  await expect(page.locator("#song-title")).toHaveText("my poem");
  await expect(page.locator("#line")).toHaveText("alpha");
  await expect(page.locator("#progress")).toHaveText("1 / 3");
});

test("uploading an empty file shows an error and keeps the current song", async ({ page }) => {
  const titleBefore = await page.locator("#song-title").textContent();

  await page.locator("#file-input").setInputFiles({
    name: "blank.txt",
    mimeType: "text/plain",
    buffer: Buffer.from("   \n\n  \n"),
  });

  await expect(page.locator("#line")).toContainText("Couldn't load that file");
  await expect(page.locator("#song-title")).toHaveText(titleBefore);
});

// --- YOUR CONTRIBUTION BELOW ----------------------------------------------
// Auto-play advances the line on a setInterval(lineDurationMs) timer. Testing
// it is a real design choice — see the request in the chat.
test("Play auto-advances lines over time", async ({ page }) => {
  // TODO(you): implement this test. ~5-10 lines.
});

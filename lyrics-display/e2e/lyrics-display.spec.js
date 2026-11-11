import { readFileSync } from 'node:fs';
import { test, expect } from '@playwright/test';
import { parseLyrics } from '../lyrics.js';

// These tests describe what the app does, not how it looks. Elements are found by
// role, accessible name and visible text only (no ids, classes or DOM structure),
// so the page can be restyled or restructured without touching this file.
//
// The contract a design must keep:
//   - the current line is the page's status region
//   - progress reads "Line N of M" and is exposed as a progressbar
//   - buttons are named Previous, Next, and Play or Pause

// Expectations come from the real lyrics.txt, so the suite keeps working when the
// file is swapped for other lyrics (it needs at least three lines).
const LINES = parseLyrics(readFileSync(new URL('../lyrics.txt', import.meta.url), 'utf8'));
const LAST = LINES.length - 1;
const DELAY_MS = 2000;

const ui = page => ({
  line: page.getByRole('status'),
  progress: page.getByRole('progressbar'),
  previous: page.getByRole('button', { name: /previous/i }),
  next: page.getByRole('button', { name: /next/i }),
  play: page.getByRole('button', { name: 'Play', exact: true }),
  pause: page.getByRole('button', { name: 'Pause', exact: true }),
  playOrPause: page.getByRole('button', { name: /^(play|pause)$/i }),
});

async function expectOnLine(page, index, total = LINES.length, text = LINES[index]) {
  const { line, progress } = ui(page);
  await expect(line).toHaveText(text);
  await expect(line).toBeInViewport(); // on screen, not just present in the page
  await expect(page.getByText(`Line ${index + 1} of ${total}`, { exact: true })).toBeVisible();
  await expect(progress).toHaveAttribute('aria-valuenow', String(index + 1));
  await expect(progress).toHaveAttribute('aria-valuemax', String(total));
}

async function open(page) {
  await page.goto('/');
  await expectOnLine(page, 0);
}

// Opens the page with a clock the test controls, so auto-play tests don't wait real seconds.
async function openWithFakeClock(page) {
  const start = new Date('2026-01-01T12:00:00');
  await page.clock.install({ time: start });
  await open(page);
  await page.clock.pauseAt(new Date(start.getTime() + 60_000));
}

async function goToLastLine(page) {
  for (let i = 0; i < LAST; i++) await page.keyboard.press('ArrowRight');
  await expectOnLine(page, LAST);
}

test.describe('initial view', () => {
  test('starts on the first line, ready to play', async ({ page }) => {
    await open(page);
    const { play, previous, next } = ui(page);
    await expect(play).toBeEnabled();
    await expect(previous).toBeDisabled();
    await expect(next).toBeEnabled();
  });
});

test.describe('stepping with the buttons', () => {
  test('Next and Previous move one line at a time', async ({ page }) => {
    await open(page);
    const { next, previous } = ui(page);

    await next.click();
    await expectOnLine(page, 1);
    await expect(previous).toBeEnabled();
    await next.click();
    await expectOnLine(page, 2);

    await previous.click();
    await previous.click();
    await expectOnLine(page, 0);
    await expect(previous).toBeDisabled();
  });

  test('Next is disabled on the last line', async ({ page }) => {
    await open(page);
    await goToLastLine(page);
    const { next, previous } = ui(page);
    await expect(next).toBeDisabled();
    await expect(previous).toBeEnabled();
  });
});

test.describe('auto-play', () => {
  test('Play becomes Pause and advances every 2 seconds', async ({ page }) => {
    await openWithFakeClock(page);
    const { play, pause } = ui(page);

    await play.click();
    await expect(pause).toBeVisible();

    await page.clock.runFor(DELAY_MS - 1);
    await expectOnLine(page, 0);
    await page.clock.runFor(1);
    await expectOnLine(page, 1);
    await page.clock.runFor(DELAY_MS);
    await expectOnLine(page, 2);
  });

  test('Pause holds the current line', async ({ page }) => {
    await openWithFakeClock(page);
    const { play, pause } = ui(page);

    await play.click();
    await page.clock.runFor(DELAY_MS);
    await pause.click();
    await expect(play).toBeVisible();

    await page.clock.runFor(DELAY_MS * 5);
    await expectOnLine(page, 1);
  });

  test('stops on the last line and offers Play again', async ({ page }) => {
    await openWithFakeClock(page);
    const { play, next } = ui(page);

    await play.click();
    await page.clock.runFor(DELAY_MS * (LINES.length + 5));
    await expectOnLine(page, LAST);
    await expect(play).toBeVisible();
    await expect(next).toBeDisabled();
  });

  test('Play on the last line restarts from the top', async ({ page }) => {
    await openWithFakeClock(page);
    await goToLastLine(page);
    const { play, pause } = ui(page);

    await play.click();
    await expectOnLine(page, 0);
    await expect(pause).toBeVisible();
    await page.clock.runFor(DELAY_MS);
    await expectOnLine(page, 1);
  });

  test('stepping manually cancels playback', async ({ page }) => {
    await openWithFakeClock(page);
    const { play, next } = ui(page);

    await play.click();
    await next.click();
    await expect(play).toBeVisible();
    await page.clock.runFor(DELAY_MS * 5);
    await expectOnLine(page, 1);
  });
});

test.describe('keyboard shortcuts', () => {
  test('arrow keys step forward and back', async ({ page }) => {
    await open(page);
    await page.keyboard.press('ArrowRight');
    await page.keyboard.press('ArrowRight');
    await expectOnLine(page, 2);
    await page.keyboard.press('ArrowLeft');
    await expectOnLine(page, 1);
  });

  test('ArrowLeft on the first line does nothing', async ({ page }) => {
    await open(page);
    await page.keyboard.press('ArrowLeft');
    await expectOnLine(page, 0);
  });

  test('Space toggles play and pause', async ({ page }) => {
    await openWithFakeClock(page);
    const { play, pause } = ui(page);

    await page.keyboard.press('Space');
    await expect(pause).toBeVisible();
    await page.clock.runFor(DELAY_MS);
    await expectOnLine(page, 1);

    await page.keyboard.press('Space');
    await expect(play).toBeVisible();
  });

  // After a mouse click, Space is still the play/pause shortcut: it must not
  // repeat the button that was just clicked.
  test('Space after clicking Next only toggles playback', async ({ page }) => {
    await openWithFakeClock(page);
    const { next, pause } = ui(page);

    await next.click();
    await page.keyboard.press('Space');
    await expect(pause).toBeVisible();
    await expectOnLine(page, 1);
  });

  // A button reached with the keyboard behaves like any button: Space activates it.
  test('Space on a keyboard-focused Next advances one line', async ({ page }) => {
    await openWithFakeClock(page);
    const { next, play } = ui(page);

    await next.focus();
    await page.keyboard.press('Space');
    await expectOnLine(page, 1);
    await expect(play).toBeVisible(); // playback did not start
  });

  test('Space on a keyboard-focused Play toggles exactly once', async ({ page }) => {
    await openWithFakeClock(page);
    const { playOrPause, pause } = ui(page);

    await playOrPause.focus();
    await page.keyboard.press('Space');
    await expect(pause).toBeVisible();
  });

  test('modified arrow keys are left to the browser', async ({ page }) => {
    await open(page);
    await page.keyboard.press('Alt+ArrowRight');
    await expectOnLine(page, 0);
  });
});

test.describe('loading problems', () => {
  async function expectControlsDisabled(page) {
    const { previous, next, playOrPause } = ui(page);
    for (const button of [previous, playOrPause, next]) await expect(button).toBeDisabled();
  }

  test('a missing lyrics file shows an error and disables the controls', async ({ page }) => {
    await page.route('**/lyrics.txt', route => route.fulfill({ status: 404, body: 'Not found' }));
    await page.goto('/');

    await expect(ui(page).line).toContainText(/could not load/i);
    await expectControlsDisabled(page);
  });

  test('an empty lyrics file shows an error and disables the controls', async ({ page }) => {
    await page.route('**/lyrics.txt', route => route.fulfill({ status: 200, contentType: 'text/plain', body: '\n \n' }));
    await page.goto('/');

    await expect(ui(page).line).toContainText(/empty/i);
    await expectControlsDisabled(page);
  });

  test('blank lines and surrounding spaces in the file are ignored', async ({ page }) => {
    await page.route('**/lyrics.txt', route =>
      route.fulfill({ status: 200, contentType: 'text/plain', body: '  first  \n\n\nsecond\n' }));
    await page.goto('/');

    await expectOnLine(page, 0, 2, 'first');
    await ui(page).next.click();
    await expectOnLine(page, 1, 2, 'second');
  });

  test('a one-line file is shown with nothing to play or step through', async ({ page }) => {
    await page.route('**/lyrics.txt', route =>
      route.fulfill({ status: 200, contentType: 'text/plain', body: 'only line\n' }));
    await page.goto('/');

    await expectOnLine(page, 0, 1, 'only line');
    await expectControlsDisabled(page);
  });
});

test.describe('layout', () => {
  async function expectNoSidewaysScroll(page) {
    const overflow = await page.evaluate(() => document.documentElement.scrollWidth - window.innerWidth);
    expect(overflow).toBeLessThanOrEqual(0);
  }

  test('fits a phone screen without scrolling', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 667 });
    await open(page);
    const { previous, playOrPause, next, progress } = ui(page);

    await expectNoSidewaysScroll(page);
    for (const control of [previous, playOrPause, next, progress]) await expect(control).toBeInViewport();
  });

  // A very long word wraps onto many rows, so the page may grow taller than the
  // screen. That is fine; it must never grow wider.
  test('a long file with a very long word never scrolls sideways on a phone', async ({ page }) => {
    const body = ['x'.repeat(300), ...Array.from({ length: 149 }, (_, i) => `line ${i + 2}`)].join('\n');
    await page.route('**/lyrics.txt', route => route.fulfill({ status: 200, contentType: 'text/plain', body }));
    await page.setViewportSize({ width: 375, height: 667 });
    await page.goto('/');
    await expect(page.getByText('Line 1 of 150', { exact: true })).toBeVisible();

    await expectNoSidewaysScroll(page);
    const { next, progress } = ui(page);
    await next.click(); // Playwright scrolls it into view first, as a user would
    await expect(page.getByText('Line 2 of 150', { exact: true })).toBeVisible();
    await expect(progress).toBeInViewport();
    await expectNoSidewaysScroll(page);
  });
});

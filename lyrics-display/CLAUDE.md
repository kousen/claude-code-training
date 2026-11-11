# Lyrics Display

A static web app that shows the lines of `lyrics.txt` one at a time, with Previous, Play/Pause and Next. See `README.md` for user-facing documentation.

## Commands

```bash
npm start                              # serve on http://localhost:8000 (python3 -m http.server)
npm test                               # Jest unit tests (lyrics.test.js)
npm run test:e2e                       # Playwright UI tests: chrome, firefox, android, ios
npx playwright test --project=chrome   # one browser only
npx playwright show-report             # open the HTML report from the last UI run
```

Run both test suites after any change to `index.html` or `lyrics.js`.

## Architecture

- `lyrics.js` holds all playback state and timing (`parseLyrics`, `createPlayer`). It must not touch the DOM, so it stays testable under Node.
- `index.html` holds markup, CSS, `render()` and event wiring only. New behavior goes in `lyrics.js`, with a unit test.
- Keyboard shortcuts call `click()` on the matching button. Do not give keys their own logic.
- No build step, no bundler, no runtime dependencies. Keep it that way; `devDependencies` are for tests only.

## Conventions

- ES modules throughout (`"type": "module"`).
- Jest runs with `--experimental-vm-modules`; import `jest` from `@jest/globals` in tests that need it.
- Jest ignores `e2e/`; Playwright only looks in `e2e/`.

## UI test rules

The Playwright tests check behavior, not appearance, so the page can be restyled freely.

- Locate elements by role, accessible name or visible text. Never by id, class or DOM structure.
- Do not assert on colors, fonts, the tick strip, the dimmed neighboring lines or number formatting.
- Read expected lyric text from `lyrics.txt`; do not hardcode lines.
- Use `page.clock` for anything timed. Never wait in real time.

A redesign must keep this contract, or the tests fail:

1. The current line is the page's `role="status"` region.
2. The text "Line N of M" is visible.
3. Progress is a `progressbar` with `aria-valuenow` and `aria-valuemax`.
4. Buttons are named Previous, Next, and Play or Pause.

## Gotchas

- The page does not work from `file://`; it needs the HTTP server for `fetch` and module scripts.
- The UI tests start their own server on port 8321, separate from the dev server on 8000.
- Space handling is deliberate. On a keyboard-focused button, Space is left to the button (standard behavior). Buttons blur after a mouse or touch click, so Space afterwards is the play/pause shortcut and does not repeat the click. Three UI tests cover this.
- On `file://` the module import itself fails, so the page stays on "Loading…" and the fetch error message never shows.
- `android` and `ios` are emulations in Chromium and WebKit, not real devices.
- `playwright-report/` and `test-results/` are generated and git-ignored.
- `lyrics.txt` is Shakespeare's Sonnet 18, which is in the public domain.

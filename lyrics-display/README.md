# Lyrics Display

A small web app that shows the lines of a text file one at a time, teleprompter style. The current line is large, with the previous and next lines dimmed around it. It ships with Shakespeare's Sonnet 18 in `lyrics.txt`.

There is no build step and no runtime dependency: one HTML page, one JavaScript module and a text file.

## Run it

```bash
npm start        # python3 -m http.server 8000
```

Then open <http://localhost:8000>.

The page must be served over HTTP. Opening `index.html` directly from disk fails, because browsers block module scripts and `fetch` on `file://` URLs; the page stays on "Loading…".

Requirements: Python 3 for the server. Node.js 18 or later is needed only for the tests.

## Use it

| Control | Button | Key |
|---|---|---|
| Next line | Next | → |
| Previous line | Previous | ← |
| Play or pause | Play / Pause | Space |

- **Play** advances one line every 2 seconds and stops on the last line. Pressing Play on the last line restarts from the top.
- Stepping manually while playing stops playback.
- The header shows "Line N of M", and the strip above the buttons has one tick per line.
- Space is the play/pause shortcut everywhere except on a button you have tabbed to, where it activates that button as usual.
- A file with a single line is displayed with all three buttons disabled.
- With many lines the progress ticks narrow and then merge into a continuous bar.
- Holding an arrow key scrubs through the lines. Arrow keys with Cmd, Ctrl or Alt held are left to the browser.

## Use your own lyrics

Replace `lyrics.txt`. Each non-blank line of the file is one displayed line; blank lines and leading or trailing spaces are ignored. Refresh the page to load the new file.

The Playwright tests read their expected text from `lyrics.txt`, so they keep working with other lyrics as long as the file has at least three lines.

## Files

| File | Purpose |
|---|---|
| `index.html` | Markup, styles, rendering and event wiring |
| `lyrics.js` | Playback logic with no DOM access: `parseLyrics` and `createPlayer` |
| `lyrics.txt` | The text to display |
| `lyrics.test.js` | Jest unit tests for `lyrics.js` |
| `e2e/lyrics-display.spec.js` | Playwright UI tests |
| `playwright.config.js` | Browsers, test server and reporters |

### How it fits together

`lyrics.js` owns the state. `createPlayer(lines, onChange, delayMs = 2000)` returns an object with:

- `index`: the current line, zero-based
- `playing`: whether auto-play is running
- `next()`, `previous()`, `toggle()`

It calls `onChange` after every change. `index.html` fetches `lyrics.txt`, passes the parsed lines and its `render` function to `createPlayer`, and connects the buttons and keys to the three methods. Keyboard shortcuts call `click()` on the matching button, so keys and buttons always behave the same.

Because the logic never touches the page, it runs under Node for the unit tests.

## Tests

```bash
npm install
npx playwright install chromium firefox webkit   # first time only

npm test                       # Jest unit tests
npm run test:e2e               # Playwright UI tests, all four browsers
npx playwright show-report     # open the HTML report from the last UI run
```

### Unit tests

`lyrics.test.js` covers parsing and the player: stepping, clamping at both ends, the 2-second auto-advance, pause, stopping on the last line, restarting from the top, and manual steps cancelling playback. Timing uses Jest's fake timers, so nothing waits in real time.

The suite runs as ES modules, which is why the `test` script passes `--experimental-vm-modules` to Node.

### UI tests

The Playwright suite runs each test in four projects:

| Project | What runs |
|---|---|
| `chrome` | Playwright's Chromium with a desktop Chrome profile |
| `firefox` | Playwright's Firefox |
| `android` | Chromium emulating a Pixel 7 |
| `ios` | WebKit emulating an iPhone 15 |

The two mobile projects are emulations (viewport, touch, user agent), not real devices.

Run one project with `npx playwright test --project=firefox`.

The tests start their own server on port 8321, so they do not need, or interfere with, a dev server on port 8000. Auto-play tests use Playwright's `page.clock` to control the browser's timers.

Each run writes an HTML report to `playwright-report/` and keeps a trace for any failing test in `test-results/`. Both folders are git-ignored.

### The contract the UI tests rely on

The UI tests check behavior, not appearance. They find elements only by role, accessible name and visible text, never by id, class or DOM structure, so the page can be restyled or restructured without editing them. A redesign must keep these four things:

1. The current line is the page's status region (`role="status"`).
2. Progress is visible as the text "Line N of M".
3. Progress is exposed as a `progressbar` with `aria-valuenow` and `aria-valuemax`.
4. The buttons have the accessible names Previous, Next, and Play or Pause.

The tests deliberately do not check the tick strip, the dimmed neighboring lines, the zero-padded line number, colors or fonts. Those can change, or break, without a test failing.

## Design

The look is a dark teleprompter: Space Grotesk type, an amber accent, a line-number gutter and a tick strip for progress.

- **Colors** are CSS custom properties on `:root` in `index.html`.
- **Fonts** load from Google Fonts. Offline, the page falls back to the system sans-serif and still works.
- **Layout** scales with the viewport; below about 480 px the buttons share the full width.
- **Motion:** each new line fades in. This is skipped when the operating system's reduced-motion setting is on.
- **Screen readers** hear only the current line; the neighboring lines are hidden from them.

The header reads "Lyrics Display" rather than a song title, because the app knows only the contents of `lyrics.txt`.

## Limitations

- Auto-play stops at the end; it does not loop.
- The 2-second delay is fixed in code (the `delayMs` default in `lyrics.js`); there is no control for it in the page.
- `python3 -m http.server` is a development server and listens on all network interfaces by default. Add `--bind 127.0.0.1` to keep it local.

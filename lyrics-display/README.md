# Lyrics Display

A single-page web app that shows a text one line at a time, with Previous / Next / Play controls. Built in Lab 0 of the Claude Code training course as the seed of the larger `lyrics-trainer` project.

## Files

- `index.html` — markup, styles, and the DOM wiring. No build step; the browser loads `player.js` as an ES module.
- `player.js` — the state machine: `parseLines`, `createPlayer` (next / prev / play / stop), and `handleKey`. No DOM access, so it is unit-testable.
- `lyrics.txt` — the text to display, one line per row. Blank lines are skipped. Ships with Shakespeare's Sonnet 18 (public domain).
- `tests/` — Vitest unit tests for `player.js`.
- `e2e/` — one Playwright smoke test that loads the page in a browser.

The app itself has no runtime dependencies. The dev dependencies in `package.json` are only for tests.

## Running

The page loads `lyrics.txt` with `fetch`, so it must be served over HTTP (opening the file directly fails with a message saying so).

```bash
python3 -m http.server 8000   # or: pnpm start
```

Then open <http://localhost:8000>.

## Controls

| Action | Button | Key |
|--------|--------|-----|
| Previous line | Previous | ← |
| Next line | Next | → |
| Play / Pause (2 s per line) | Play | Space |

Play stops at the last line; pressing Play again restarts from the top. A counter below the line shows the current position (e.g. `3 / 14`).

## Testing

```bash
pnpm install          # once
pnpm test             # unit tests (Vitest, fake timers)
pnpm coverage         # same, with a coverage table for player.js
pnpm test:e2e         # browser smoke test; starts its own server on port 8765
```

The first `pnpm test:e2e` needs a browser: `pnpm exec playwright install chromium`.

The unit tests cover every branch of `player.js`: line parsing, bounds on Previous / Next, auto-advance timing, stopping at the end, restarting from the top, pause clearing the timer exactly once, and the keyboard mapping. The smoke test checks that the page serves, loads the lyrics, and advances one line after Space.

## Changing the text

Replace `lyrics.txt` with any plain-text file, one line per row. Prefer public-domain material; copyrighted song lyrics tend to trip content filters during Claude Code demos.

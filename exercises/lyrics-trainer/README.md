# Lyrics Trainer

A single-page app that displays song lyrics one line at a time, for memorization
practice. No build step, no framework — plain HTML/CSS/JS with a small ES module
for the testable logic.

## Run it

Browsers block `fetch()` on `file://` URLs, so serve the folder over HTTP:

```bash
python3 -m http.server 8000
```

Then open http://localhost:8000. Put your lyrics in `lyrics.txt` (one line of
the song per line of the file).

## Controls

- **Next / Previous** buttons, or **← / →** arrow keys
- **Play / Pause** button, or **Spacebar** — auto-advances one line every N
  seconds (2s by default; adjust with the delay slider)

## Tests

`lyrics-logic.js` holds the pure logic (line parsing, navigation bounds,
label/progress formatting) with no DOM dependency, so it's unit tested
directly with [Vitest](https://vitest.dev):

```bash
pnpm install
pnpm test
```

## Files

| File                    | Purpose                                              |
|-------------------------|-------------------------------------------------------|
| `index.html`             | UI, styling, DOM wiring, keyboard shortcuts           |
| `lyrics-logic.js`        | Pure functions: parsing, navigation, formatting       |
| `lyrics-logic.test.js`   | Vitest unit tests for `lyrics-logic.js`               |
| `lyrics.txt`              | The lyrics displayed by the app (one line per line)   |

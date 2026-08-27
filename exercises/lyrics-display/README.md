# Lyrics Display

A single-page app that shows song lyrics one line at a time, with manual
navigation and auto-play. Built during Lab 0 of the Claude Code training
course — no framework, no build step, no dependencies.

## Run

Browsers block `fetch` on `file://` URLs, so serve the folder over HTTP:

```bash
python3 -m http.server 8080
```

Then open <http://localhost:8080>.

## Use

| Action            | Button       | Keys                      |
|-------------------|--------------|---------------------------|
| Previous / Next   | ‹ Previous / Next › | ← / →              |
| Play / Pause      | Play         | Space or K                |
| First / last line | —            | ⌘← / ⌘→ (or Fn+arrow, Home/End) |
| Playback speed    | Speed slider | 0.5 s – 5 s per line      |

Navigation wraps around at both ends; Play loops until paused.

## Change the lyrics

Replace `lyrics.txt` with any plain-text file, one lyric line per line.
Blank lines are ignored.

## Test

```bash
node --test
```

Tests cover the pure logic in `app.js` (lyric parsing and index wrap-around).
The DOM wiring lives inline in `index.html`.

## Files

- `index.html` — markup, styles, and DOM/event code
- `app.js` — pure functions (`parseLyrics`, `wrap`), shared with the tests
- `app.test.js` — `node:test` suite
- `lyrics.txt` — the lyrics to display

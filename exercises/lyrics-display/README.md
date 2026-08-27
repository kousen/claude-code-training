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
| Recall mode       | Recall menu  | see below                 |
| Reveal masked line| click line   | R                         |

Navigation wraps around at both ends; Play loops until paused.

## Recall mode (memorisation practice)

The **Recall** menu masks parts of each line so you can test yourself:

1. **Show everything** — plain display
2. **Hide every other word** — `the _____ brown ___`
3. **First word only** — `the _____ _____ ___`
4. **Hide all** — `___ _____ _____ ___`

Hidden words keep their length. Click the line or press **R** to reveal it.

## Change the lyrics

Replace `lyrics.txt` with any plain-text file, one lyric line per line.
Blank lines are ignored.

## Test

```bash
node --test
```

Tests cover the pure logic in `app.js` (lyric parsing, index wrap-around, and recall masking).
The DOM wiring lives inline in `index.html`.

## Files

- `index.html` — markup, styles, and DOM/event code
- `app.js` — pure functions (`parseLyrics`, `wrap`, `maskLine`), shared with the tests
- `app.test.js` — `node:test` suite
- `lyrics.txt` — the lyrics to display

# Lyrics Display

A single-page web app that shows a text one line at a time, with Previous / Next / Play controls. Built in Lab 0 of the Claude Code training course as the seed of the larger `lyrics-trainer` project.

## Files

- `index.html` — the whole app: markup, styles, and script. No build step, no dependencies.
- `lyrics.txt` — the text to display, one line per row. Blank lines are skipped. Ships with Shakespeare's Sonnet 18 (public domain).

## Running

The page loads `lyrics.txt` with `fetch`, so it must be served over HTTP (opening the file directly fails with a message saying so).

```bash
python3 -m http.server 8000
```

Then open <http://localhost:8000>.

## Controls

| Action | Button | Key |
|--------|--------|-----|
| Previous line | Previous | ← |
| Next line | Next | → |
| Play / Pause (2 s per line) | Play | Space |

Play stops at the last line; pressing Play again restarts from the top. A counter below the line shows the current position (e.g. `3 / 14`).

## Changing the text

Replace `lyrics.txt` with any plain-text file, one line per row. Prefer public-domain material; copyrighted song lyrics tend to trip content filters during Claude Code demos.

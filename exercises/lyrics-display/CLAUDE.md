# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A dependency-free lyrics player: `lyrics.txt` shown one line at a time, with Previous/Play/Next.
This is the finished reference version of the course's **Lab 0**, where students build it from an
empty directory. The repo-root `CLAUDE.md` covers the course as a whole.

## Commands

```bash
python3 -m http.server 8000                          # run it: http://localhost:8000
node --test                                          # all tests (Node's built-in runner; nothing to install)
node --test --test-name-pattern="Windows"            # one test, matched by name
node --test --experimental-test-coverage             # with coverage
```

There is no build, lint or `package.json`.

## Architecture

- **`lyrics.js`** holds the page-independent logic: `parseLyrics(text)` returns `{ title, lines }` and
  `durationFor(line)` returns the auto-play wait in ms. It's the only part with unit tests.
- **`index.html`** holds everything else: styles, markup, and the display/controls script.
- **`lyrics.js` must stay a classic script, not an ES module.** The page loads it with a plain
  `<script src>` because browsers block modules on `file://`, and the "Open another text…" file
  picker exists so the page works when double-clicked. Its last line
  (`if (typeof module === 'object') module.exports = …`) lets the tests `require()` it.
  That works only because the nearest `package.json` (at the repo root) sets no `"type"`. If it
  ever becomes `"module"`, rename the test to `.cjs`.
- **State in `index.html`** is four variables: `lines`, `index`, `timer` (non-null means playing)
  and `shown`. `render()` is the single place the DOM updates. It animates the verse only when
  `index !== shown`, so Play/Pause toggles don't replay the crossfade.
- **Auto-play is a chain of `setTimeout` calls** (`scheduleNext`), not `setInterval`, because each
  line's wait comes from `durationFor`. It stops on the last line.
- **Keyboard shortcuts call the buttons' `.click()`**, so disabled buttons and "manual navigation
  stops auto-play" apply to both. `preventDefault` on the keydown stops Space from also activating
  a focused button.
- **`load(text)` and `fail(message)`** are the only ways content changes. `load` parses first,
  so a bad file throws before any state changes. `fail` clears the title, ticks and numeral and
  disables the controls. `render()` re-enables Play.
  The startup `fetch('lyrics.txt')` is ignored once the user has picked a file (`picked` flag),
  so a slow `lyrics.txt` can't overwrite or error over their choice.
- **The margin numeral is a CSS counter** (`counter-set` from JS, `lower-roman` in CSS). The
  `.verse.blank` class hides it before a load or after a failure, and is removed inside the view-
  transition swap so the old snapshot never shows "0.".

## Lyrics file format

An optional first line starting with `#` is the title, split on ` — ` into the running head's
left (title) and right (byline). Blank lines are skipped, and CRLF is handled. The bundled
`lyrics.txt` is public-domain Donne. One test asserts its title and 13-line length, so update
`lyrics.test.js` if you change it.

## Verifying UI changes

The display and controls code has no automated tests. Check changes in a real browser at desktop
width and at 390px (no horizontal scroll), and exercise Space and the arrow keys with a button
focused.

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
npm start                          # serve on http://localhost:8000 (python3 -m http.server)
npm install                        # first time only; the only dependency is Jest (dev)
npm test                           # run all tests
npm test -- -t "pause holds"       # run tests whose name matches a pattern
```

There is no build step and no linter. The app must be served over HTTP
because `index.html` fetches `lyrics.txt`; opening the file directly fails.

Use npm in this folder, not pnpm. The repository root is a separate pnpm
project (the Slidev deck) and this folder is deliberately independent of it.

## Architecture

The app is split so that all behaviour is testable without a browser:

- `lyrics.js` is a DOM-free ES module. `parseLyrics(text)` turns the file
  into an array of non-blank, trimmed lines. `createPlayer(count, onChange,
  delayMs = 2000)` owns the current index and the play timer, and returns
  `{ next, prev, toggle }`. It never touches the page: after every state
  change it calls `onChange({ index, playing, direction })`, where
  `direction` is `1`, `-1`, or `0` when the line did not change.
- `index.html` holds the markup, the CSS, and a `<script type="module">`
  that fetches `lyrics.txt`, creates the player, and implements `render`,
  the single function that maps a state object onto the page (text, line
  number, progress bar, button states, the `playing` class on `<body>`,
  and the line transition).

Put new behaviour in `lyrics.js` with a test, and keep `index.html` to
rendering. State flows one way: button click → player method → `onChange`
→ `render`.

## Testing

Tests are in `lyrics.test.js` and use Jest with native ES modules. That
requires the `--experimental-vm-modules` flag (already in the `test` script)
and `"transform": {}` in the Jest config in `package.json`. Because of ES
module mode, `jest`, `test`, `expect` and friends are imported from
`@jest/globals` rather than used as globals.

Timer behaviour is tested with `jest.useFakeTimers()` and
`jest.advanceTimersByTime()`; do not add real waits.

`index.html` has no automated tests. After changing it, check the page in a
browser at both desktop and phone widths.

## Conventions

- No runtime dependencies and no frameworks; plain HTML, CSS and JavaScript.
- Colors and font stacks are CSS variables on `:root` in `index.html`.
- Honour `prefers-reduced-motion` for any new animation, in CSS and in JS.
- `lyrics.txt` holds the lyrics text; code should work for any plain-text
  file and must not depend on its contents or line count.

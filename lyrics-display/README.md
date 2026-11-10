# Lyrics Display

A small web app that shows the lines of `lyrics.txt` one at a time, with
Previous, Play/Pause and Next buttons. Play advances automatically every
2 seconds.

The bundled `lyrics.txt` is Shakespeare's Sonnet 18 (public domain). Replace
it with any plain-text file, one line of lyrics per text line.

## Run it

The page loads `lyrics.txt` with `fetch`, which browsers block on `file://`
URLs, so serve the folder over HTTP:

```bash
npm start          # runs: python3 -m http.server 8000
```

Then open <http://localhost:8000/>. Any static file server works; nothing
needs to be built or installed to run the app.

## Features

- **Previous / Next** step one line and disable at the first and last line.
- **Play** advances every 2 seconds and becomes **Pause** while active.
  Playback stops on the last line; pressing Play there restarts from the top.
- **Line number and progress**: a "Line 3" label, a progress bar, and a
  "3 of 14 · 21%" counter.
- **Transitions**: each new line fades and slides in, upward when moving
  forward and downward when moving back.
- **Responsive**: three buttons in a row on wide screens; on phones Play
  takes a full-width row above Previous and Next.
- **Reduced motion**: animations are skipped when the system "reduce motion"
  setting is on.

## Test it

```bash
npm install        # first time only; installs Jest
npm test
```

Requires Node.js 18.14 or later. Node prints an "ExperimentalWarning: VM
Modules" line on each run; that is expected, because Jest's ES module support
uses the `--experimental-vm-modules` flag.

The tests cover line parsing and the player logic (stepping, the 2-second
auto-advance, pause, end-of-song and replay). The display code in
`index.html` is not covered by automated tests.

## Files

| File | Purpose |
|------|---------|
| `index.html` | Markup, styles, and the code that draws the current state on the page |
| `lyrics.js` | Logic with no browser dependencies: `parseLyrics` and `createPlayer` |
| `lyrics.test.js` | Jest tests for `lyrics.js` |
| `lyrics.txt` | The lyrics, one line per text line; blank lines are skipped |
| `package.json` | `start` and `test` scripts, Jest configuration |

## Customize

- **Different lyrics**: edit or replace `lyrics.txt` and reload the page.
- **Delay**: pass a third argument to `createPlayer` in `index.html`, in
  milliseconds: `createPlayer(lines.length, render, 3000)`.
- **Loop instead of stopping**: see the comment in `tick()` in `lyrics.js`.
  The "play stops by itself after the last line" test will then need updating.
- **Colors and fonts**: the CSS variables at the top of the `<style>` block in
  `index.html`.

## Browser support

The page uses ES modules, `text-wrap: balance`, `backdrop-filter` and the Web
Animations API, so it needs a current version of Chrome, Edge, Firefox or
Safari.

# Lyrics Display

Shows song lyrics one line at a time, with Previous/Next navigation and an auto-play mode.
It's plain HTML, CSS and JavaScript, with no build step and nothing to install.

This is a reference version of **Lab 0** (building a project from scratch). In the lab, students create
their own version in an empty directory, so theirs will differ. It's also where the
[`lyrics-trainer`](../javascript/lyrics-trainer) project started.

## Design

The look is a **broadside**, styled after a single printed sheet. It uses warm paper, black ink and
one printer's red, with the lyric set in IM Fell English, a revival of 17th-century type.
- The title and author form a running head over a double rule. A title written as
  `Song — Artist` is split between the left and right sides.
- The line number hangs in the margin as a red roman numeral (`iii.`), drawn by a CSS counter with
  `lower-roman`, so no JavaScript converts the numbers.
- The next line is previewed in italic below the current one.
- A row of tick marks, one per line, fills in with ink as the song goes on.

## Running it

Either of these works:

- **Serve the folder** (loads `lyrics.txt` automatically):
  ```bash
  python3 -m http.server 8000
  # then open http://localhost:8000
  ```
- **Double-click `index.html`**, then click **Open another text…** and choose a `.txt` file.
  Browsers block pages opened from disk (`file://`) from fetching `lyrics.txt`,
  but a file you choose yourself is read directly.

## Controls

| Action | Button | Key |
|---|---|---|
| Play / pause auto-advance | ▶ Play / ❚❚ Pause | `Space` |
| Next line | Next ▶ | `→` |
| Previous line | ◀ Previous | `←` |

- Clicking Next or Previous (or pressing an arrow key) stops auto-play.
- Auto-play stops on the last line. Pressing Play there starts again from line 1.

## Lyrics file format

Plain text, one lyric line per line:

```text
# Song Title — Artist
First line of the song
Second line

Blank lines (stanza breaks) are skipped
```

- A first line starting with `#` is the **title**. It's shown as a heading and in the browser tab,
  and isn't counted as a lyric line. It's optional.
- Blank lines are ignored, and Windows (`\r\n`) line endings are handled.

The included `lyrics.txt` is the passage from John Donne's *Meditation XVII* (1624) that begins
"No man is an island…". It's in the public domain.

## Auto-play timing

Each line stays on screen for **1 s + 0.3 s per word**, with a **1.5 s minimum**, so long
lines get more reading time. To change the pace, adjust `BASE_MS`, `MS_PER_WORD` and `MIN_MS`
in `lyrics.js`.

## Files

| File | Contents |
|---|---|
| `index.html` | Page, styles, and display/controls logic |
| `lyrics.js` | Page-independent logic: `parseLyrics()` and `durationFor()` |
| `lyrics.test.js` | Tests for `lyrics.js` |
| `lyrics.txt` | Sample lyrics |

`lyrics.js` is loaded with a plain `<script>` tag, not as an ES module, because browsers block
modules on `file://` pages. Its last line exports the functions when it runs under Node.

## Tests

```bash
node --test                               # run the tests
node --test --experimental-test-coverage  # with a coverage report
```

These use Node's built-in test runner and need Node 20+. There's nothing to install.

The tests cover `lyrics.js`: title detection, blank-line skipping, CRLF input, rejection of empty
files, line timing, and the bundled `lyrics.txt`. That's 100% of `lyrics.js`, but only about
11% of the app's JavaScript. The display and controls code in `index.html` has no automated tests
and was checked by hand in a browser.

## Browser notes

- Line changes crossfade using the **View Transitions API**. Browsers without it swap lines instantly.
- Animations are turned off when the system's **reduce motion** setting is on.
- Fonts (IM Fell English, IM Fell English SC, EB Garamond) load from Google Fonts. Offline, the
  page falls back to Georgia.
- IM Fell uses old-style figures, so numbers sit like lower-case letters and "13" can look like
  "i3". That's authentic to the period, but not ideal for projection.

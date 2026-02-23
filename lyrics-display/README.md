# Lyrics Display

A web application that displays song lyrics one line at a time with playback controls.

## Features

- **Line-by-line display** with smooth fade/blur transitions
- **Playback controls**: Previous, Play/Pause, Next
- **Auto-advance**: Play mode steps through lines on a 2-second interval
- **Keyboard shortcuts**: `←` previous, `→` next, `Space` play/pause
- **Progress bar** showing position in the song
- Responsive design for mobile and desktop

## Getting Started

The app loads lyrics from `lyrics.txt` via `fetch`, so it needs a local server:

```bash
npm install
python3 -m http.server 8000
```

Then open http://localhost:8000.

## Running Tests

```bash
npm test            # single run
npm run test:watch  # watch mode
```

## Project Structure

```
index.html              HTML + CSS + imports app.js as ES module
app.js                  DOM wiring, rendering, timer management
lyrics-player.js        Pure state logic (no DOM dependencies)
lyrics-player.test.js   Vitest tests for LyricsPlayer
lyrics.txt              Song lyrics (one line per line, blank lines ignored)
```

## Architecture

`LyricsPlayer` is a pure state machine with no side effects — it tracks the current line index and play/pause state. `app.js` is a thin UI layer that connects the player to the DOM and manages the `setInterval` timer. This separation keeps all business logic testable without a browser environment.

## Using Different Lyrics

Replace the contents of `lyrics.txt`. Each non-empty line becomes one display step.

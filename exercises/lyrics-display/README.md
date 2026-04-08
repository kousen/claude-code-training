# Lyrics Display

A web application that displays song lyrics one line at a time with playback controls.

## Features

- **Line-by-line display** with smooth fade and blur transitions
- **Play/Pause** auto-advances through lines every 2 seconds
- **Previous/Next** buttons for manual navigation
- **Keyboard shortcuts**: Arrow keys to navigate, Spacebar to play/pause
- **Progress bar** with line counter (e.g., "Line 5 of 12")
- **Responsive design** adapts to mobile, tablet, and desktop

## Project Structure

```text
lyrics-display/
├── index.html          # Page structure and layout
├── styles.css          # All styling, animations, and responsive breakpoints
├── app.js              # DOM interaction, rendering, and event handling
├── lyrics-state.js     # Pure state logic (navigation, progress, play/pause)
├── lyrics-state.test.js# Unit tests for the state module
├── lyrics.txt          # Song lyrics (one line per line, blank lines ignored)
└── package.json        # Project config and test script
```

## Quick Start

Serve the files with any static server (required for `fetch` to load `lyrics.txt`):

```bash
python3 -m http.server 8000
```

Then open <http://localhost:8000>.

## Running Tests

```bash
npm install
npm test
```

## Using Your Own Lyrics

Replace the contents of `lyrics.txt`. Each non-blank line becomes one display step. Blank lines are ignored.

## Architecture

The app separates concerns into two layers:

- **`lyrics-state.js`** is a pure state module with no DOM dependencies. It manages the current index, navigation boundaries, progress percentage, and play/pause state. This is the layer covered by unit tests.
- **`app.js`** is the thin rendering layer that reads from the state module and updates the DOM. It handles animations, timers, and event listeners.

This separation means the core logic can be tested in milliseconds without any browser or DOM setup.

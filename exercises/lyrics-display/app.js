import { SONG } from "./lyrics.js";
import { nextIndex, prevIndex, parseLyrics, formatDuration } from "./player.js";

// --- State -----------------------------------------------------------------
let currentSong = SONG;   // the song being displayed; swapped on file upload
let currentIndex = 0;     // which line of currentSong.lines is showing
let playTimer = null;     // setInterval handle while auto-playing; null when paused
let lineDurationMs = SONG.lineDurationMs; // auto-play delay; user-adjustable

// --- DOM references --------------------------------------------------------
const titleEl = document.getElementById("song-title");
const lineEl = document.getElementById("line");
const progressEl = document.getElementById("progress");
const progressFillEl = document.getElementById("progress-fill");
const prevBtn = document.getElementById("prev-btn");
const playBtn = document.getElementById("play-btn");
const nextBtn = document.getElementById("next-btn");
const fileInput = document.getElementById("file-input");
const speedInput = document.getElementById("speed-input");
const speedValueEl = document.getElementById("speed-value");

// --- Rendering -------------------------------------------------------------
// Redraws the whole UI from currentIndex. Call this after any state change.
function render() {
  titleEl.textContent = currentSong.title;
  lineEl.textContent = currentSong.lines[currentIndex];

  const lineNumber = currentIndex + 1;
  const lineCount = currentSong.lines.length;
  progressEl.textContent = `${lineNumber} / ${lineCount}`;
  progressFillEl.style.width = `${(lineNumber / lineCount) * 100}%`;

  const isPlaying = playTimer !== null;
  // Static literals only — the arrow glyph is aria-hidden so screen readers
  // read just "Play" / "Pause", consistent with the prev/next buttons.
  playBtn.innerHTML = isPlaying
    ? '<span aria-hidden="true">⏸</span> Pause'
    : '<span aria-hidden="true">▶</span> Play';
  playBtn.classList.toggle("playing", isPlaying);
  playBtn.setAttribute("aria-pressed", isPlaying);
}

// Shows a message in the line area instead of a lyric. Leaves currentSong
// untouched; the aria-live region announces the message like any line change.
function showError(message) {
  titleEl.textContent = currentSong.title;
  lineEl.textContent = message;
}

// Restarts the auto-play interval from now. Called when starting playback and
// after a manual jump, so the freshly-shown line gets a full duration.
function startTimer() {
  clearInterval(playTimer);
  playTimer = setInterval(() => {
    currentIndex = nextIndex(currentIndex, currentSong.lines.length);
    render();
  }, lineDurationMs);
}

function stopPlayback() {
  clearInterval(playTimer);
  playTimer = null;
}

// --- Speed -----------------------------------------------------------------
// Updates the auto-play delay. While playing, restarts the timer so the new
// speed takes effect immediately instead of after the current line finishes.
function setSpeed(ms) {
  lineDurationMs = ms;
  speedValueEl.textContent = formatDuration(ms);
  speedInput.setAttribute("aria-valuetext", `${formatDuration(ms)} per line`);
  if (playTimer !== null) startTimer();
}

// --- Navigation ------------------------------------------------------------
// nextIndex / prevIndex come from player.js (pure, unit-tested wrap logic).
function next() {
  currentIndex = nextIndex(currentIndex, currentSong.lines.length);
  if (playTimer !== null) startTimer();
  render();
}

function previous() {
  currentIndex = prevIndex(currentIndex, currentSong.lines.length);
  if (playTimer !== null) startTimer();
  render();
}

// --- Play / Pause ----------------------------------------------------------
function togglePlay() {
  if (playTimer !== null) {
    stopPlayback();
  } else {
    startTimer();
  }
  render();
}

// --- File loading ----------------------------------------------------------
// Reads an uploaded .txt file, parses it, and swaps it in as the current song.
// On any failure the current song is left intact and an error is shown.
function loadFile(file) {
  const reader = new FileReader();

  reader.onload = () => {
    try {
      const song = parseLyrics(reader.result, file.name);
      stopPlayback();          // the old song's timer/length no longer apply
      currentSong = song;
      currentIndex = 0;
      render();
    } catch (err) {
      showError(`Couldn't load that file — ${err.message}`);
    }
  };

  reader.onerror = () => {
    showError("Couldn't read that file. Try a different one.");
  };

  reader.readAsText(file);
}

// --- Wiring ----------------------------------------------------------------
prevBtn.addEventListener("click", previous);
nextBtn.addEventListener("click", next);
playBtn.addEventListener("click", togglePlay);
fileInput.addEventListener("change", () => {
  const file = fileInput.files[0];
  if (file) loadFile(file);
  fileInput.value = ""; // let the same file be re-selected later
});
speedInput.addEventListener("input", () => setSpeed(Number(speedInput.value)));

// Sync the slider's thumb and label to the starting delay.
speedInput.value = lineDurationMs;
setSpeed(lineDurationMs);

// Keyboard shortcuts. Skip when a button or the file input has focus, so we
// don't double-fire (Space already "clicks" a focused button, for example).
document.addEventListener("keydown", (event) => {
  const tag = event.target.tagName;
  if (tag === "BUTTON" || tag === "INPUT") return;

  if (event.code === "Space") {
    event.preventDefault(); // Space would otherwise scroll the page
    togglePlay();
  } else if (event.code === "ArrowRight") {
    next();
  } else if (event.code === "ArrowLeft") {
    previous();
  }
});

render();

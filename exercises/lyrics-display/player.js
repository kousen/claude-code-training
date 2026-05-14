// Pure index math for walking the lyrics array — no DOM, no state.
// Kept separate from app.js so it can be unit-tested with `node --test`.

// Next line, wrapping past the end back to 0.
export function nextIndex(i, len) {
  return (i + 1) % len;
}

// Previous line, wrapping past 0 back to the end.
// The `+ len` keeps the result non-negative — JS `%` keeps the dividend's sign.
export function prevIndex(i, len) {
  return (i - 1 + len) % len;
}

// Auto-play duration used for songs that carry no duration of their own
// (e.g. plain-text files the user uploads).
export const DEFAULT_LINE_DURATION_MS = 3500;

// Formats a millisecond duration as a short seconds label: 3500 -> "3.5s".
export function formatDuration(ms) {
  return `${ms / 1000}s`;
}

// Turns the raw text of an uploaded .txt file into a song object.
// Pure: no DOM, no state — so it can be unit-tested like nextIndex/prevIndex.
// Throws if the file has no usable lines, so the caller can show an error.
export function parseLyrics(text, filename) {
  const lines = text
    .replace(/\r\n?/g, "\n") // normalize Windows/old-Mac line endings
    .split("\n")
    .map((line) => line.trim())
    .filter((line) => line.length > 0);

  if (lines.length === 0) {
    throw new Error("it has no readable lines.");
  }

  const title =
    filename.replace(/\.txt$/i, "").replace(/[-_]+/g, " ").trim() || "Untitled";

  return { title, lineDurationMs: DEFAULT_LINE_DURATION_MS, lines };
}

// Pure functions only — no DOM. Kept separate so they can be unit tested directly.

export function parseLyrics(text) {
  return text.split('\n').map(l => l.trim()).filter(Boolean);
}

export function canGoPrev(index) {
  return index > 0;
}

export function canGoNext(index, length) {
  return index < length - 1;
}

export function lineLabel(index) {
  return `Line ${index + 1}`;
}

export function counterLabel(index, length) {
  return `Line ${index + 1} of ${length}`;
}

export function progressPercent(index, length) {
  return ((index + 1) / length) * 100;
}

export function secondsToMs(seconds) {
  return Number(seconds) * 1000;
}

export function formatDelay(seconds) {
  return `${Number(seconds).toFixed(1)}s`;
}

// Run with: node --test
import test from "node:test";
import assert from "node:assert/strict";
import {
  nextIndex,
  prevIndex,
  parseLyrics,
  formatDuration,
  DEFAULT_LINE_DURATION_MS,
} from "./player.js";

test("nextIndex advances by one", () => {
  assert.equal(nextIndex(0, 4), 1);
  assert.equal(nextIndex(2, 4), 3);
});

test("nextIndex wraps past the end back to 0", () => {
  assert.equal(nextIndex(3, 4), 0);
});

test("prevIndex steps back by one", () => {
  assert.equal(prevIndex(3, 4), 2);
  assert.equal(prevIndex(1, 4), 0);
});

test("prevIndex wraps past 0 back to the end", () => {
  assert.equal(prevIndex(0, 4), 3);
});

test("single-line song stays put in both directions", () => {
  assert.equal(nextIndex(0, 1), 0);
  assert.equal(prevIndex(0, 1), 0);
});

test("parseLyrics splits text into one entry per line", () => {
  const song = parseLyrics("first line\nsecond line\nthird line", "poem.txt");
  assert.deepEqual(song.lines, ["first line", "second line", "third line"]);
  assert.equal(song.lineDurationMs, DEFAULT_LINE_DURATION_MS);
});

test("parseLyrics normalizes CRLF and bare CR line endings", () => {
  const song = parseLyrics("a\r\nb\rc", "x.txt");
  assert.deepEqual(song.lines, ["a", "b", "c"]);
});

test("parseLyrics trims whitespace and drops blank lines", () => {
  const song = parseLyrics("  padded  \n\n\n   \nlast", "x.txt");
  assert.deepEqual(song.lines, ["padded", "last"]);
});

test("parseLyrics throws when there are no usable lines", () => {
  assert.throws(() => parseLyrics("   \n\n  \r\n", "blank.txt"), /no readable lines/);
});

test("parseLyrics derives a title from the filename", () => {
  assert.equal(parseLyrics("x", "sonnet-18.txt").title, "sonnet 18");
  assert.equal(parseLyrics("x", "my_song.TXT").title, "my song");
  assert.equal(parseLyrics("x", ".txt").title, "Untitled");
});

test("formatDuration renders milliseconds as a seconds label", () => {
  assert.equal(formatDuration(3500), "3.5s");
  assert.equal(formatDuration(1000), "1s");
  assert.equal(formatDuration(500), "0.5s");
});

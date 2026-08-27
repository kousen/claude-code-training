const { test } = require('node:test');
const assert = require('node:assert/strict');
const { parseLyrics, parseTitle, wrap, maskLine } = require('./app.js');

test('parseLyrics drops blank lines and trims', () => {
  assert.deepEqual(parseLyrics('a\n\n  b  \n\t\nc\n'), ['a', 'b', 'c']);
});

test('wrap loops around both ends', () => {
  assert.equal(wrap(5, 5), 0);    // next past last
  assert.equal(wrap(-1, 5), 4);   // prev before first
  assert.equal(wrap(2, 5), 2);
  assert.equal(wrap(0, 0), 0);    // empty lyrics, no NaN
});

test('maskLine hides progressively more, preserving word lengths', () => {
  const l = 'the quick brown fox';
  assert.equal(maskLine(l, 0), 'the quick brown fox');
  assert.equal(maskLine(l, 1), 'the _____ brown ___');
  assert.equal(maskLine(l, 2), 'the _____ _____ ___');
  assert.equal(maskLine(l, 3), '___ _____ _____ ___');
});

test('parseTitle reads an optional "# Title" first line; parseLyrics skips it', () => {
  assert.equal(parseTitle('# My Song\nline one'), 'My Song');
  assert.equal(parseTitle('line one\nline two'), null);
  assert.deepEqual(parseLyrics('# My Song\nline one'), ['line one']);
});

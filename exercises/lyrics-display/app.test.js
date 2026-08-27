const { test } = require('node:test');
const assert = require('node:assert/strict');
const { parseLyrics, wrap } = require('./app.js');

test('parseLyrics drops blank lines and trims', () => {
  assert.deepEqual(parseLyrics('a\n\n  b  \n\t\nc\n'), ['a', 'b', 'c']);
});

test('wrap loops around both ends', () => {
  assert.equal(wrap(5, 5), 0);    // next past last
  assert.equal(wrap(-1, 5), 4);   // prev before first
  assert.equal(wrap(2, 5), 2);
  assert.equal(wrap(0, 0), 0);    // empty lyrics, no NaN
});

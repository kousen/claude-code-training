const { test } = require('node:test');
const assert = require('node:assert/strict');
const { parseLyrics, clamp } = require('./app.js');

test('parseLyrics drops blank lines and trims', () => {
  assert.deepEqual(parseLyrics('a\n\n  b  \n\t\nc\n'), ['a', 'b', 'c']);
});

test('clamp keeps index within [0, len-1]', () => {
  assert.equal(clamp(-1, 5), 0);
  assert.equal(clamp(5, 5), 4);
  assert.equal(clamp(2, 5), 2);
  assert.equal(clamp(-10, 5), 0);  // Home
  assert.equal(clamp(99, 5), 4);   // End
});

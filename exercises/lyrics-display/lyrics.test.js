// Run with: node --test   (Node's built-in runner; nothing to install)
const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { parseLyrics, durationFor } = require('./lyrics.js');

test('a leading "#" line becomes the title, not a lyric', () => {
  assert.deepEqual(parseLyrics('# Song — Artist\nFirst\nSecond'),
    { title: 'Song — Artist', lines: ['First', 'Second'] });
});

test('without a "#" line there is no title and every line is a lyric', () => {
  assert.deepEqual(parseLyrics('First\nSecond'), { title: '', lines: ['First', 'Second'] });
});

test('blank and whitespace-only lines (stanza breaks) are skipped', () => {
  assert.deepEqual(parseLyrics('\nFirst\n\n   \nSecond\n\n').lines, ['First', 'Second']);
});

test('Windows line endings leave no stray \\r on the lines', () => {
  assert.deepEqual(parseLyrics('# Title\r\nFirst\r\nSecond\r\n'),
    { title: 'Title', lines: ['First', 'Second'] });
});

test('files with no lyric lines are rejected', () => {
  for (const text of ['', '\n\n', '# Only a title\n']) {
    assert.throws(() => parseLyrics(text), /no lyric lines found/, JSON.stringify(text));
  }
});

test('longer lines stay on screen longer, with a minimum for very short ones', () => {
  assert.equal(durationFor('one two three'), 1900);      // 1000 + 3 × 300
  assert.equal(durationFor('  one   two   three  '), 1900); // extra spaces aren't extra words
  assert.equal(durationFor('Hey'), 1500);                // 1300 would be too quick, so the floor applies
});

test('the bundled lyrics.txt parses into a titled, 13-line poem', () => {
  const { title, lines } = parseLyrics(fs.readFileSync(path.join(__dirname, 'lyrics.txt'), 'utf8'));
  assert.equal(title, 'No Man Is an Island — John Donne (1624)');
  assert.equal(lines.length, 13);
});

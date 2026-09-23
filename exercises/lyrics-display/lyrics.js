// Page-independent lyric logic, kept separate so it can be tested.
// Loaded as a classic <script>, not a module: browsers block modules on pages opened from disk (file://).

// Auto-play time per line scales with its length: a base pause plus reading time per word
const BASE_MS = 1000, MS_PER_WORD = 300, MIN_MS = 1500;
const durationFor = line => Math.max(MIN_MS, BASE_MS + MS_PER_WORD * line.trim().split(/\s+/).length);

// An optional first line starting with "#" is the title, e.g. "# Song Title — Artist"
function parseLyrics(text) {
  const lines = text.split(/\r?\n/).filter(l => l.trim()); // skip blank stanza breaks
  const title = lines[0]?.startsWith('#') ? lines.shift().replace(/^#+\s*/, '') : '';
  if (!lines.length) throw new Error('no lyric lines found');
  return { title, lines };
}

// Lets Node's test runner require() this file; ignored in the browser
if (typeof module === 'object') module.exports = { parseLyrics, durationFor };

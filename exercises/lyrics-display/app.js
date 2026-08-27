// Pure logic, shared by index.html and app.test.js
const parseLyrics = text => text.split('\n').map(l => l.trim()).filter(Boolean);
const clamp = (idx, len) => Math.min(Math.max(idx, 0), len - 1);
if (typeof module !== 'undefined') module.exports = { parseLyrics, clamp };

// Pure logic, shared by index.html and app.test.js
const parseLyrics = text => text.split('\n').map(l => l.trim()).filter(Boolean);
const wrap = (idx, len) => len ? ((idx % len) + len) % len : 0;
if (typeof module !== 'undefined') module.exports = { parseLyrics, wrap };

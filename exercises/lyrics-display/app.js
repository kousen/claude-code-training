// Pure logic, shared by index.html and app.test.js
const parseLyrics = text => text.split('\n').map(l => l.trim()).filter(l => l && !l.startsWith('#'));
// Optional first line "# Song Title" names the song; null if absent.
const parseTitle = text => { const m = text.match(/^\s*#\s*(.+)/); return m ? m[1].trim() : null; };
const wrap = (idx, len) => len ? ((idx % len) + len) % len : 0;
// Recall levels: 0 = full line, 1 = hide every other word, 2 = first word only, 3 = all hidden.
// Hidden words keep their length so the line's shape stays recognisable.
const hide = w => w.replace(/[^\s]/g, '_');
const maskLine = (line, level) =>
  line.split(' ').map((w, j) => {
    if (level === 0) return w;
    if (level === 1) return j % 2 ? hide(w) : w;
    if (level === 2) return j === 0 ? w : hide(w);
    return hide(w);
  }).join(' ');
if (typeof module !== 'undefined') module.exports = { parseLyrics, parseTitle, wrap, maskLine };

// Lyrics player state machine. No DOM, no real timers: everything is injected so it runs in tests.

export function parseLines(text) {
  return text.split('\n').filter(l => l.trim());
}

export function createPlayer(lines, {
  delay = 2000,
  onChange = () => {},
  setInterval = globalThis.setInterval,
  clearInterval = globalThis.clearInterval,
} = {}) {
  let i = 0, timer = null;
  const last = () => lines.length - 1;
  const changed = () => onChange(i, lines[i]);

  function next() {
    if (i < last()) { i++; changed(); } else stop();
  }
  function prev() {
    if (i > 0) { i--; changed(); }
  }
  function stop() {
    if (timer === null) return;
    clearInterval(timer);
    timer = null;
    changed();                       // let the UI redraw the Play/Pause label
  }
  function play() {
    if (timer !== null) return stop();
    if (i === last()) { i = 0; }     // restart from the top when already at the end
    timer = setInterval(next, delay);
    changed();
  }

  return {
    next, prev, play, stop,
    get index() { return i; },
    get line() { return lines[i]; },
    get length() { return lines.length; },
    get isPlaying() { return timer !== null; },
  };
}

// Maps a keydown event to a player action; returns true when it handled the key.
export function handleKey(player, event) {
  const action = { ArrowLeft: player.prev, ArrowRight: player.next, ' ': player.play }[event.key];
  if (!action) return false;
  event.preventDefault();            // stop Space from also clicking a focused button
  action();
  return true;
}

// Playback logic for the lyrics display, kept free of the DOM so it can be tested in Node.

export function parseLyrics(text) {
  return text.split('\n').map(line => line.trim()).filter(Boolean);
}

export function createPlayer(lines, onChange = () => {}, delayMs = 2000) {
  const last = lines.length - 1;
  let index = 0;
  let timer = null;

  function stop() {
    clearInterval(timer);
    timer = null;
  }

  function move(delta) {
    index = Math.min(Math.max(index + delta, 0), last);
    if (index === last) stop(); // auto-play ends on the last line
    onChange();
  }

  return {
    get index() { return index; },
    get playing() { return timer !== null; },
    next() { stop(); move(1); },
    previous() { stop(); move(-1); },
    toggle() {
      if (timer) {
        stop();
      } else if (last > 0) { // a single line has nothing to play through
        if (index === last) index = 0; // replay from the top
        timer = setInterval(() => move(1), delayMs);
      }
      onChange();
    },
  };
}

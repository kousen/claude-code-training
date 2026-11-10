// Logic for the lyrics display, kept free of the DOM so it can be tested in Node.

export function parseLyrics(text) {
  return text.split(/\r?\n/).map(line => line.trim()).filter(Boolean);
}

// onChange receives { index, playing, direction } after every state change.
// direction is 1 (moved forward), -1 (moved back) or 0 (line did not change).
export function createPlayer(count, onChange, delayMs = 2000) {
  let index = 0;
  let timer = null;
  const last = count - 1;
  const emit = (direction = 0) => onChange({ index, playing: timer !== null, direction });

  function go(delta) {
    const target = Math.max(0, Math.min(last, index + delta));
    if (target === index) return;
    index = target;
    emit(delta);
  }

  function stop() {
    clearInterval(timer);
    timer = null;
    emit();
  }

  function tick() {
    // End-of-song behaviour: stop on the last line. To loop instead,
    // replace the body with: index = (index + 1) % count; emit(1);
    if (index === last) return stop();
    go(1);
  }

  function toggle() {
    if (timer !== null) return stop();
    const restart = index === last; // replay from the top
    if (restart) index = 0;
    timer = setInterval(tick, delayMs);
    emit(restart ? 1 : 0);
  }

  emit();
  return { next: () => go(1), prev: () => go(-1), toggle };
}

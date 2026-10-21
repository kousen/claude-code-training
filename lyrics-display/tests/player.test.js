import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { parseLines, createPlayer, handleKey } from '../player.js';

const LINES = ['one', 'two', 'three'];

describe('parseLines', () => {
  it('splits on newlines and drops blank lines, including a trailing newline', () => {
    expect(parseLines('a\n\nb\n   \nc\n')).toEqual(['a', 'b', 'c']);
  });
});

describe('navigation', () => {
  it('starts on the first line', () => {
    const p = createPlayer(LINES);
    expect(p.index).toBe(0);
    expect(p.line).toBe('one');
    expect(p.length).toBe(3);
  });

  it('next advances and stops at the last line', () => {
    const p = createPlayer(LINES);
    p.next(); p.next(); p.next(); p.next();
    expect(p.index).toBe(2);
  });

  it('prev retreats and stops at zero', () => {
    const p = createPlayer(LINES);
    p.next(); p.prev(); p.prev();
    expect(p.index).toBe(0);
  });

  it('fires onChange with index and line on real moves only', () => {
    const onChange = vi.fn();
    const p = createPlayer(LINES, { onChange });
    p.prev();                              // no-op at zero
    expect(onChange).not.toHaveBeenCalled();
    p.next();
    expect(onChange).toHaveBeenCalledWith(1, 'two');
    expect(onChange).toHaveBeenCalledTimes(1);
  });
});

describe('play', () => {
  beforeEach(() => vi.useFakeTimers());
  afterEach(() => vi.useRealTimers());

  it('advances one line per tick', () => {
    const p = createPlayer(LINES, { delay: 2000 });
    p.play();
    expect(p.isPlaying).toBe(true);
    vi.advanceTimersByTime(2000);
    expect(p.index).toBe(1);
    vi.advanceTimersByTime(1999);
    expect(p.index).toBe(1);
    vi.advanceTimersByTime(1);
    expect(p.index).toBe(2);
  });

  it('stops itself after reaching the last line', () => {
    const p = createPlayer(LINES, { delay: 100 });
    p.play();
    vi.advanceTimersByTime(300);
    expect(p.index).toBe(2);
    expect(p.isPlaying).toBe(false);
    vi.advanceTimersByTime(1000);
    expect(p.index).toBe(2);             // nothing left running
  });

  it('pauses when called while playing, clearing the timer exactly once', () => {
    const clearSpy = vi.spyOn(globalThis, 'clearInterval');
    const p = createPlayer(LINES, { delay: 100 });
    p.play();
    p.play();                             // pause
    expect(p.isPlaying).toBe(false);
    p.stop();                             // already stopped: must not clear again
    expect(clearSpy).toHaveBeenCalledTimes(1);
    vi.advanceTimersByTime(500);
    expect(p.index).toBe(0);
  });

  it('restarts from the top when played at the last line', () => {
    const p = createPlayer(LINES, { delay: 100 });
    p.next(); p.next();
    expect(p.index).toBe(2);
    p.play();
    expect(p.index).toBe(0);
    expect(p.isPlaying).toBe(true);
    vi.advanceTimersByTime(100);
    expect(p.index).toBe(1);
  });

  it('notifies onChange on play and on pause so the UI can relabel the button', () => {
    const onChange = vi.fn();
    const p = createPlayer(LINES, { onChange });
    p.play();
    p.play();
    expect(onChange).toHaveBeenCalledTimes(2);
  });
});

describe('handleKey', () => {
  const event = key => ({ key, preventDefault: vi.fn() });

  it('maps Space, ArrowLeft and ArrowRight and prevents the default', () => {
    const p = { prev: vi.fn(), next: vi.fn(), play: vi.fn() };
    for (const [key, fn] of [[' ', 'play'], ['ArrowLeft', 'prev'], ['ArrowRight', 'next']]) {
      const e = event(key);
      expect(handleKey(p, e)).toBe(true);
      expect(p[fn]).toHaveBeenCalledTimes(1);
      expect(e.preventDefault).toHaveBeenCalledTimes(1);
    }
  });

  it('ignores other keys without preventing their default', () => {
    const p = { prev: vi.fn(), next: vi.fn(), play: vi.fn() };
    const e = event('Enter');
    expect(handleKey(p, e)).toBe(false);
    expect(e.preventDefault).not.toHaveBeenCalled();
  });
});

import { afterEach, beforeEach, describe, expect, jest, test } from '@jest/globals';
import { createPlayer, parseLyrics } from './lyrics.js';

describe('parseLyrics', () => {
  test('splits text into trimmed lines', () => {
    expect(parseLyrics('  one \ntwo\n')).toEqual(['one', 'two']);
  });

  test('skips blank lines and handles Windows line endings', () => {
    expect(parseLyrics('one\r\n\r\n   \r\ntwo')).toEqual(['one', 'two']);
  });

  test('returns an empty list for empty text', () => {
    expect(parseLyrics('')).toEqual([]);
  });
});

describe('createPlayer', () => {
  let states;
  let player;
  const latest = () => states.at(-1);

  beforeEach(() => {
    jest.useFakeTimers();
    states = [];
    player = createPlayer(3, state => states.push(state));
  });

  afterEach(() => jest.useRealTimers());

  test('starts paused on the first line', () => {
    expect(states).toEqual([{ index: 0, playing: false, direction: 0 }]);
  });

  test('next and prev move one line and report the direction', () => {
    player.next();
    expect(latest()).toEqual({ index: 1, playing: false, direction: 1 });
    player.prev();
    expect(latest()).toEqual({ index: 0, playing: false, direction: -1 });
  });

  test('does nothing past either end', () => {
    player.prev();
    expect(states).toHaveLength(1);
    player.next();
    player.next();
    player.next();
    expect(latest().index).toBe(2);
    expect(states).toHaveLength(3);
  });

  test('play advances one line every 2 seconds', () => {
    player.toggle();
    expect(latest()).toEqual({ index: 0, playing: true, direction: 0 });
    jest.advanceTimersByTime(1999);
    expect(latest().index).toBe(0);
    jest.advanceTimersByTime(1);
    expect(latest().index).toBe(1);
    jest.advanceTimersByTime(2000);
    expect(latest().index).toBe(2);
  });

  test('pause holds the current line', () => {
    player.toggle();
    jest.advanceTimersByTime(2000);
    player.toggle();
    expect(latest()).toEqual({ index: 1, playing: false, direction: 0 });
    jest.advanceTimersByTime(10000);
    expect(latest().index).toBe(1);
  });

  test('play stops by itself after the last line', () => {
    player.toggle();
    jest.advanceTimersByTime(6000);
    expect(latest()).toEqual({ index: 2, playing: false, direction: 0 });
    expect(jest.getTimerCount()).toBe(0);
  });

  test('play on the last line restarts from the top', () => {
    player.next();
    player.next();
    player.toggle();
    expect(latest()).toEqual({ index: 0, playing: true, direction: 1 });
  });

  test('honours a custom delay', () => {
    const seen = [];
    createPlayer(2, state => seen.push(state), 500).toggle();
    jest.advanceTimersByTime(500);
    expect(seen.at(-1).index).toBe(1);
  });
});

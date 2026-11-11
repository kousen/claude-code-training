import { jest } from '@jest/globals';
import { parseLyrics, createPlayer } from './lyrics.js';

const LINES = ['one', 'two', 'three'];

describe('parseLyrics', () => {
  test('splits into trimmed lines and drops blank ones', () => {
    expect(parseLyrics('  one \n\ntwo\r\n   \nthree\n')).toEqual(LINES);
  });

  test('returns no lines for empty text', () => {
    expect(parseLyrics('\n  \n')).toEqual([]);
  });
});

describe('stepping', () => {
  test('starts on the first line, not playing', () => {
    const player = createPlayer(LINES);
    expect(player.index).toBe(0);
    expect(player.playing).toBe(false);
  });

  test('next and previous move one line', () => {
    const player = createPlayer(LINES);
    player.next();
    expect(player.index).toBe(1);
    player.previous();
    expect(player.index).toBe(0);
  });

  test('does not move past either end', () => {
    const player = createPlayer(LINES);
    player.previous();
    expect(player.index).toBe(0);
    player.next(); player.next(); player.next();
    expect(player.index).toBe(2);
  });

  test.each(['next', 'previous', 'toggle'])('%s notifies the listener', action => {
    const onChange = jest.fn();
    const player = createPlayer(LINES, onChange);
    player.next(); // start on the middle line so every action has somewhere to go
    onChange.mockClear();
    player[action]();
    expect(onChange).toHaveBeenCalled();
    if (player.playing) player.toggle(); // don't leave a real interval running
  });
});

describe('auto-play', () => {
  beforeEach(() => jest.useFakeTimers());
  afterEach(() => jest.useRealTimers());

  test('advances one line every 2 seconds', () => {
    const player = createPlayer(LINES);
    player.toggle();
    expect(player.playing).toBe(true);
    jest.advanceTimersByTime(1999);
    expect(player.index).toBe(0);
    jest.advanceTimersByTime(1);
    expect(player.index).toBe(1);
  });

  test('toggle pauses', () => {
    const player = createPlayer(LINES);
    player.toggle();
    jest.advanceTimersByTime(2000);
    player.toggle();
    jest.advanceTimersByTime(10000);
    expect(player.playing).toBe(false);
    expect(player.index).toBe(1);
  });

  test('stops on the last line', () => {
    const player = createPlayer(LINES);
    player.toggle();
    jest.advanceTimersByTime(60000);
    expect(player.index).toBe(2);
    expect(player.playing).toBe(false);
  });

  test('play on the last line restarts from the top', () => {
    const player = createPlayer(LINES);
    player.next(); player.next();
    player.toggle();
    expect(player.index).toBe(0);
    expect(player.playing).toBe(true);
  });

  test('stepping manually stops playback', () => {
    const player = createPlayer(LINES);
    player.toggle();
    player.next();
    expect(player.playing).toBe(false);
    jest.advanceTimersByTime(10000);
    expect(player.index).toBe(1);
  });

  test('does not start for a single line', () => {
    const player = createPlayer(['only']);
    player.toggle();
    expect(player.playing).toBe(false);
    expect(player.index).toBe(0);
  });

  test('honors a custom delay', () => {
    const player = createPlayer(LINES, undefined, 500);
    player.toggle();
    jest.advanceTimersByTime(500);
    expect(player.index).toBe(1);
  });
});

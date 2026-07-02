import { describe, it, expect } from 'vitest';
import {
  parseLyrics, canGoPrev, canGoNext, lineLabel,
  counterLabel, progressPercent, secondsToMs, formatDelay,
} from './lyrics-logic.js';

describe('parseLyrics', () => {
  it('trims and drops blank lines', () => {
    expect(parseLyrics('  hello  \n\nworld\n  \n')).toEqual(['hello', 'world']);
  });

  it('handles Windows line endings', () => {
    expect(parseLyrics('a\r\nb\r\n')).toEqual(['a', 'b']);
  });

  it('returns an empty array for empty input', () => {
    expect(parseLyrics('')).toEqual([]);
  });
});

describe('canGoPrev / canGoNext', () => {
  it('blocks prev at the first line', () => {
    expect(canGoPrev(0)).toBe(false);
    expect(canGoPrev(1)).toBe(true);
  });

  it('blocks next at the last line', () => {
    expect(canGoNext(2, 3)).toBe(false);
    expect(canGoNext(1, 3)).toBe(true);
  });

  it('blocks both ways on a single-line song', () => {
    expect(canGoPrev(0)).toBe(false);
    expect(canGoNext(0, 1)).toBe(false);
  });
});

describe('labels', () => {
  it('lineLabel is 1-indexed', () => {
    expect(lineLabel(0)).toBe('Line 1');
    expect(lineLabel(4)).toBe('Line 5');
  });

  it('counterLabel shows position and total', () => {
    expect(counterLabel(4, 32)).toBe('Line 5 of 32');
  });
});

describe('progressPercent', () => {
  it('is proportional to 1-indexed position', () => {
    expect(progressPercent(0, 4)).toBe(25);
    expect(progressPercent(3, 4)).toBe(100);
  });
});

describe('delay conversions', () => {
  it('secondsToMs converts slider value to milliseconds', () => {
    expect(secondsToMs('2')).toBe(2000);
    expect(secondsToMs('0.5')).toBe(500);
  });

  it('formatDelay renders one decimal place', () => {
    expect(formatDelay('2')).toBe('2.0s');
    expect(formatDelay('0.5')).toBe('0.5s');
  });
});

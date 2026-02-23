import { describe, it, expect, beforeEach } from 'vitest';
import { LyricsPlayer } from './lyrics-player.js';

const SAMPLE_LINES = [
  'I am a lineman for the county,',
  'And I drive the main road.',
  'Searchin\' in the sun for another overload.',
  'I hear you singing in the wire.',
];

describe('LyricsPlayer', () => {
  let player;

  beforeEach(() => {
    player = new LyricsPlayer(SAMPLE_LINES);
  });

  describe('initialization', () => {
    it('starts at the first line', () => {
      expect(player.currentIndex).toBe(0);
      expect(player.currentLine).toBe(SAMPLE_LINES[0]);
    });

    it('is not playing initially', () => {
      expect(player.playing).toBe(false);
    });

    it('reports the total number of lines', () => {
      expect(player.totalLines).toBe(4);
    });
  });

  describe('next()', () => {
    it('advances to the next line', () => {
      player.next();
      expect(player.currentIndex).toBe(1);
      expect(player.currentLine).toBe(SAMPLE_LINES[1]);
    });

    it('returns true when it advances', () => {
      expect(player.next()).toBe(true);
    });

    it('does not advance past the last line', () => {
      player.currentIndex = 3;
      expect(player.next()).toBe(false);
      expect(player.currentIndex).toBe(3);
    });
  });

  describe('previous()', () => {
    it('goes back to the previous line', () => {
      player.currentIndex = 2;
      player.previous();
      expect(player.currentIndex).toBe(1);
    });

    it('returns true when it goes back', () => {
      player.currentIndex = 1;
      expect(player.previous()).toBe(true);
    });

    it('does not go before the first line', () => {
      expect(player.previous()).toBe(false);
      expect(player.currentIndex).toBe(0);
    });
  });

  describe('boundary properties', () => {
    it('isAtStart is true only at index 0', () => {
      expect(player.isAtStart).toBe(true);
      player.next();
      expect(player.isAtStart).toBe(false);
    });

    it('isAtEnd is true only at last index', () => {
      expect(player.isAtEnd).toBe(false);
      player.currentIndex = 3;
      expect(player.isAtEnd).toBe(true);
    });
  });

  describe('progress', () => {
    it('reports progress as a fraction', () => {
      expect(player.progress).toBe(1 / 4);
      player.currentIndex = 3;
      expect(player.progress).toBe(4 / 4);
    });
  });

  describe('play/pause', () => {
    it('play() sets playing to true', () => {
      player.play();
      expect(player.playing).toBe(true);
    });

    it('pause() sets playing to false', () => {
      player.play();
      player.pause();
      expect(player.playing).toBe(false);
    });

    it('togglePlay() toggles the playing state', () => {
      player.togglePlay();
      expect(player.playing).toBe(true);
      player.togglePlay();
      expect(player.playing).toBe(false);
    });

    it('play() resets to start when at the end', () => {
      player.currentIndex = 3;
      player.play();
      expect(player.currentIndex).toBe(0);
      expect(player.playing).toBe(true);
    });
  });

  describe('reset()', () => {
    it('resets index and stops playing', () => {
      player.currentIndex = 2;
      player.play();
      player.reset();
      expect(player.currentIndex).toBe(0);
      expect(player.playing).toBe(false);
    });
  });
});

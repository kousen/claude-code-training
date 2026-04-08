import { describe, it, expect } from 'vitest';
import { createLyricsState } from './lyrics-state.js';

const sampleLines = [
    'Morning light filters through the window pane,',
    'Coffee steam curling like a lazy refrain.',
    'The cat stretches out on the kitchen floor,',
    'Another day begins, just like before.',
];

describe('createLyricsState', () => {
    it('initializes at the first line', () => {
        const state = createLyricsState(sampleLines);
        expect(state.index).toBe(0);
        expect(state.currentLine).toBe(sampleLines[0]);
    });

    it('reports the total line count', () => {
        const state = createLyricsState(sampleLines);
        expect(state.lineCount).toBe(4);
    });
});

describe('navigation', () => {
    it('advances to the next line', () => {
        const state = createLyricsState(sampleLines);
        const moved = state.next();
        expect(moved).toBe(true);
        expect(state.index).toBe(1);
        expect(state.currentLine).toBe(sampleLines[1]);
    });

    it('does not advance past the last line', () => {
        const state = createLyricsState(sampleLines);
        state.goTo(3);
        const moved = state.next();
        expect(moved).toBe(false);
        expect(state.index).toBe(3);
    });

    it('goes to the previous line', () => {
        const state = createLyricsState(sampleLines);
        state.next();
        state.next();
        const moved = state.previous();
        expect(moved).toBe(true);
        expect(state.index).toBe(1);
    });

    it('does not go before the first line', () => {
        const state = createLyricsState(sampleLines);
        const moved = state.previous();
        expect(moved).toBe(false);
        expect(state.index).toBe(0);
    });

    it('jumps to a specific line with goTo', () => {
        const state = createLyricsState(sampleLines);
        state.goTo(2);
        expect(state.index).toBe(2);
        expect(state.currentLine).toBe(sampleLines[2]);
    });

    it('rejects out-of-bounds goTo values', () => {
        const state = createLyricsState(sampleLines);
        expect(state.goTo(-1)).toBe(false);
        expect(state.goTo(4)).toBe(false);
        expect(state.index).toBe(0);
    });
});

describe('boundary flags', () => {
    it('isFirst is true only at index 0', () => {
        const state = createLyricsState(sampleLines);
        expect(state.isFirst).toBe(true);
        expect(state.isLast).toBe(false);
        state.next();
        expect(state.isFirst).toBe(false);
    });

    it('isLast is true only at the final index', () => {
        const state = createLyricsState(sampleLines);
        state.goTo(3);
        expect(state.isLast).toBe(true);
        expect(state.isFirst).toBe(false);
    });
});

describe('progress', () => {
    it('reports 25% at the first of four lines', () => {
        const state = createLyricsState(sampleLines);
        expect(state.progress).toBe(25);
    });

    it('reports 50% at the second of four lines', () => {
        const state = createLyricsState(sampleLines);
        state.next();
        expect(state.progress).toBe(50);
    });

    it('reports 100% at the last line', () => {
        const state = createLyricsState(sampleLines);
        state.goTo(3);
        expect(state.progress).toBe(100);
    });
});

describe('play/pause', () => {
    it('starts in stopped state', () => {
        const state = createLyricsState(sampleLines);
        expect(state.isPlaying).toBe(false);
    });

    it('togglePlay starts playback and returns true', () => {
        const state = createLyricsState(sampleLines);
        const nowPlaying = state.togglePlay();
        expect(nowPlaying).toBe(true);
        expect(state.isPlaying).toBe(true);
    });

    it('togglePlay again pauses and returns false', () => {
        const state = createLyricsState(sampleLines);
        state.togglePlay();
        const nowPlaying = state.togglePlay();
        expect(nowPlaying).toBe(false);
        expect(state.isPlaying).toBe(false);
    });

    it('stop forces playback off', () => {
        const state = createLyricsState(sampleLines);
        state.togglePlay();
        state.stop();
        expect(state.isPlaying).toBe(false);
    });

    it('stop is safe to call when already stopped', () => {
        const state = createLyricsState(sampleLines);
        state.stop();
        expect(state.isPlaying).toBe(false);
    });
});

describe('full playthrough simulation', () => {
    it('can advance through all lines and stops at the end', () => {
        const state = createLyricsState(sampleLines);
        const visited = [state.currentLine];
        while (state.next()) {
            visited.push(state.currentLine);
        }
        expect(visited).toEqual(sampleLines);
        expect(state.isLast).toBe(true);
        expect(state.next()).toBe(false);
    });
});

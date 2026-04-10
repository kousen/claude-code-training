import { describe, it, expect } from 'vitest';
import { parseLyrics, LyricsEngine } from './lyrics-engine.js';

describe('parseLyrics', () => {
    it('splits text into non-empty lines', () => {
        const text = 'line one\nline two\nline three';
        expect(parseLyrics(text)).toEqual(['line one', 'line two', 'line three']);
    });

    it('filters out blank lines between stanzas', () => {
        const text = 'verse one\n\nverse two\n\nverse three';
        expect(parseLyrics(text)).toEqual(['verse one', 'verse two', 'verse three']);
    });

    it('handles whitespace-only lines', () => {
        const text = 'first\n   \n  \t \nsecond';
        expect(parseLyrics(text)).toEqual(['first', 'second']);
    });

    it('handles trailing newline', () => {
        const text = 'only line\n';
        expect(parseLyrics(text)).toEqual(['only line']);
    });

    it('handles Windows-style \\r\\n line endings', () => {
        const text = 'first line\r\nsecond line\r\nthird line\r\n';
        expect(parseLyrics(text)).toEqual(['first line', 'second line', 'third line']);
    });

    it('returns empty array for empty string', () => {
        expect(parseLyrics('')).toEqual([]);
    });
});

describe('LyricsEngine', () => {
    const sampleLines = ['line A', 'line B', 'line C', 'line D'];

    describe('constructor', () => {
        it('throws on empty array', () => {
            expect(() => new LyricsEngine([])).toThrow('non-empty');
        });

        it('throws on non-array', () => {
            expect(() => new LyricsEngine('not an array')).toThrow();
        });

        it('throws when parseLyrics result of empty file is passed in', () => {
            const lines = parseLyrics('');
            expect(() => new LyricsEngine(lines)).toThrow('non-empty');
        });
    });

    describe('initial state', () => {
        it('starts at the first line', () => {
            const engine = new LyricsEngine(sampleLines);
            const state = engine.state;
            expect(state.line).toBe('line A');
            expect(state.index).toBe(0);
            expect(state.isFirst).toBe(true);
            expect(state.isLast).toBe(false);
        });

        it('reports correct total', () => {
            const engine = new LyricsEngine(sampleLines);
            expect(engine.state.total).toBe(4);
        });

        it('calculates initial progress', () => {
            const engine = new LyricsEngine(sampleLines);
            expect(engine.state.progress).toBeCloseTo(0.25);
        });

        it('single-line engine is both first and last', () => {
            const engine = new LyricsEngine(['solo']);
            const state = engine.state;
            expect(state.isFirst).toBe(true);
            expect(state.isLast).toBe(true);
            expect(state.progress).toBeCloseTo(1.0);
        });
    });

    describe('next()', () => {
        it('advances to the next line', () => {
            const engine = new LyricsEngine(sampleLines);
            const state = engine.next();
            expect(state.line).toBe('line B');
            expect(state.index).toBe(1);
        });

        it('stays on the last line when called at the end', () => {
            const engine = new LyricsEngine(['only one']);
            const state = engine.next();
            expect(state.index).toBe(0);
            expect(state.line).toBe('only one');
            expect(state.isLast).toBe(true);
        });

        it('updates progress as it advances', () => {
            const engine = new LyricsEngine(sampleLines);
            engine.next();
            engine.next();
            engine.next();
            expect(engine.state.progress).toBeCloseTo(1.0);
            expect(engine.state.isLast).toBe(true);
        });
    });

    describe('prev()', () => {
        it('moves back to the previous line', () => {
            const engine = new LyricsEngine(sampleLines);
            engine.next();
            engine.next();
            const state = engine.prev();
            expect(state.line).toBe('line B');
            expect(state.index).toBe(1);
        });

        it('stays on the first line when called at the start', () => {
            const engine = new LyricsEngine(sampleLines);
            const state = engine.prev();
            expect(state.index).toBe(0);
            expect(state.line).toBe('line A');
            expect(state.isFirst).toBe(true);
        });
    });

    describe('goTo()', () => {
        it('jumps to a specific line', () => {
            const engine = new LyricsEngine(sampleLines);
            const state = engine.goTo(2);
            expect(state.line).toBe('line C');
            expect(state.index).toBe(2);
        });

        it('throws on negative index', () => {
            const engine = new LyricsEngine(sampleLines);
            expect(() => engine.goTo(-1)).toThrow(RangeError);
        });

        it('throws on index beyond last line', () => {
            const engine = new LyricsEngine(sampleLines);
            expect(() => engine.goTo(4)).toThrow(RangeError);
        });

        it('can jump to first index', () => {
            const engine = new LyricsEngine(sampleLines);
            engine.next();
            engine.next();
            const state = engine.goTo(0);
            expect(state.line).toBe('line A');
            expect(state.isFirst).toBe(true);
        });

        it('can jump to last index', () => {
            const engine = new LyricsEngine(sampleLines);
            const state = engine.goTo(3);
            expect(state.line).toBe('line D');
            expect(state.isLast).toBe(true);
        });
    });

    describe('full playthrough simulation', () => {
        it('advances through all lines and stops at the end', () => {
            const engine = new LyricsEngine(sampleLines);

            engine.next(); // -> line B (index 1)
            engine.next(); // -> line C (index 2)
            engine.next(); // -> line D (index 3)

            const state = engine.state;
            expect(state.index).toBe(3);
            expect(state.line).toBe('line D');
            expect(state.isLast).toBe(true);
            expect(state.progress).toBeCloseTo(1.0);

            // One more next() should stay put — this is how Play knows to stop
            const stuck = engine.next();
            expect(stuck.index).toBe(3);
            expect(stuck.line).toBe('line D');
        });
    });
});

/**
 * Pure state-management engine for lyrics navigation.
 * No DOM, no timers — just state and transitions.
 */

export function parseLyrics(text) {
    return text.split(/\r?\n/).filter(line => line.trim() !== '');
}

export class LyricsEngine {
    #lines;
    #index;

    constructor(lines) {
        if (!Array.isArray(lines) || lines.length === 0) {
            throw new Error('LyricsEngine requires a non-empty array of lines');
        }
        this.#lines = lines;
        this.#index = 0;
    }

    get state() {
        return {
            line: this.#lines[this.#index],
            index: this.#index,
            total: this.#lines.length,
            progress: (this.#index + 1) / this.#lines.length,
            isFirst: this.#index === 0,
            isLast: this.#index === this.#lines.length - 1,
        };
    }

    next() {
        if (this.#index < this.#lines.length - 1) {
            this.#index++;
        }
        return this.state;
    }

    prev() {
        if (this.#index > 0) {
            this.#index--;
        }
        return this.state;
    }

    goTo(index) {
        if (index < 0 || index >= this.#lines.length) {
            throw new RangeError(
                `Index ${index} out of bounds (0–${this.#lines.length - 1})`
            );
        }
        this.#index = index;
        return this.state;
    }
}

const DEFAULT_SPEED = 2000;
const MIN_SPEED = 500;
const MAX_SPEED = 5000;

export { DEFAULT_SPEED, MIN_SPEED, MAX_SPEED };

export function createLyricsState(lines) {
    let currentIndex = 0;
    let playing = false;
    let speed = DEFAULT_SPEED;

    return {
        get index() { return currentIndex; },
        get isPlaying() { return playing; },
        get lineCount() { return lines.length; },
        get currentLine() { return lines[currentIndex]; },
        get progress() { return Math.round(((currentIndex + 1) / lines.length) * 100); },
        get isFirst() { return currentIndex === 0; },
        get isLast() { return currentIndex === lines.length - 1; },
        get speed() { return speed; },

        next() {
            if (currentIndex < lines.length - 1) {
                currentIndex++;
                return true;
            }
            return false;
        },

        previous() {
            if (currentIndex > 0) {
                currentIndex--;
                return true;
            }
            return false;
        },

        goTo(index) {
            if (index >= 0 && index < lines.length) {
                currentIndex = index;
                return true;
            }
            return false;
        },

        togglePlay() {
            playing = !playing;
            return playing;
        },

        stop() {
            playing = false;
        },

        setSpeed(ms) {
            const clamped = Math.max(MIN_SPEED, Math.min(MAX_SPEED, ms));
            speed = clamped;
            return speed;
        }
    };
}

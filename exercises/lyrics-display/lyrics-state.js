export function createLyricsState(lines) {
    let currentIndex = 0;
    let playing = false;

    return {
        get index() { return currentIndex; },
        get isPlaying() { return playing; },
        get lineCount() { return lines.length; },
        get currentLine() { return lines[currentIndex]; },
        get progress() { return Math.round(((currentIndex + 1) / lines.length) * 100); },
        get isFirst() { return currentIndex === 0; },
        get isLast() { return currentIndex === lines.length - 1; },

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
        }
    };
}

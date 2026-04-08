import { createLyricsState, DEFAULT_SPEED } from './lyrics-state.js';

let state;
let playInterval = null;
let transitioning = false;

async function loadLyrics() {
    const response = await fetch('lyrics.txt');
    const text = await response.text();
    const lines = text.split('\n').filter(line => line.trim() !== '');
    state = createLyricsState(lines);
    render(false);
}

function render(animate = true) {
    const display = document.getElementById('lyric-line');

    if (animate) {
        transitioning = true;
        display.classList.add('fade-out');

        setTimeout(() => {
            display.classList.remove('fade-out');
            display.textContent = state.currentLine;
            display.classList.add('fade-in');

            requestAnimationFrame(() => {
                requestAnimationFrame(() => {
                    display.classList.remove('fade-in');
                    transitioning = false;
                });
            });
        }, 300);
    } else {
        display.textContent = state.currentLine;
    }

    document.getElementById('line-label').textContent =
        `Line ${state.index + 1}`;
    document.getElementById('status').textContent =
        `Line ${state.index + 1} of ${state.lineCount}`;
    document.getElementById('progress-bar').style.width =
        `${state.progress}%`;
    document.getElementById('pct').textContent =
        `${state.progress}%`;

    document.getElementById('prev-btn').disabled = state.isFirst;
    document.getElementById('next-btn').disabled = state.isLast;
}

function next() {
    if (transitioning) return;
    if (state.next()) {
        render();
    } else {
        stopPlay();
    }
}

function previous() {
    if (transitioning) return;
    if (state.previous()) {
        render();
    }
}

function startInterval() {
    clearInterval(playInterval);
    playInterval = setInterval(() => {
        if (!state.isLast) {
            next();
        } else {
            stopPlay();
        }
    }, state.speed);
}

function togglePlay() {
    if (playInterval) {
        stopPlay();
        return;
    }
    state.togglePlay();
    const btn = document.getElementById('play-btn');
    btn.innerHTML = '&#9646;&#9646; Pause';
    btn.classList.add('playing');
    startInterval();
}

function stopPlay() {
    clearInterval(playInterval);
    playInterval = null;
    state.stop();
    const btn = document.getElementById('play-btn');
    btn.innerHTML = '&#9654; Play';
    btn.classList.remove('playing');
}

document.getElementById('prev-btn').addEventListener('click', previous);
document.getElementById('play-btn').addEventListener('click', togglePlay);
document.getElementById('next-btn').addEventListener('click', next);

const speedSlider = document.getElementById('speed-slider');
const speedValue = document.getElementById('speed-value');

speedSlider.addEventListener('input', () => {
    const ms = state.setSpeed(Number(speedSlider.value));
    speedValue.textContent = `${(ms / 1000).toFixed(1)}s`;
    if (playInterval) {
        startInterval();
    }
});

document.addEventListener('keydown', (e) => {
    if (e.key === 'ArrowRight' || e.key === 'ArrowDown') {
        e.preventDefault();
        next();
    } else if (e.key === 'ArrowLeft' || e.key === 'ArrowUp') {
        e.preventDefault();
        previous();
    } else if (e.key === ' ') {
        e.preventDefault();
        togglePlay();
    }
});

loadLyrics();

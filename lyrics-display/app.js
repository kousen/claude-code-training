import { LyricsPlayer } from './lyrics-player.js';

const display = document.getElementById('lyrics-display');
const counter = document.getElementById('line-counter');
const progressBar = document.getElementById('progress-bar');
const prevBtn = document.getElementById('prev-btn');
const nextBtn = document.getElementById('next-btn');
const playBtn = document.getElementById('play-btn');
const playIcon = document.getElementById('play-icon');

const PLAY_SVG = '<polygon points="6,3 20,12 6,21"/>';
const PAUSE_SVG = '<rect x="5" y="3" width="4" height="18"/><rect x="15" y="3" width="4" height="18"/>';

let player;
let intervalId = null;

function render() {
  const span = display.querySelector('span');
  span.classList.add('hidden');

  setTimeout(() => {
    span.textContent = player.currentLine;
    span.classList.remove('hidden');
  }, 300);

  counter.textContent = `${player.currentIndex + 1} / ${player.totalLines}`;
  progressBar.style.width = `${player.progress * 100}%`;
  prevBtn.disabled = player.isAtStart;
  nextBtn.disabled = player.isAtEnd;

  if (player.playing) {
    playIcon.innerHTML = PAUSE_SVG;
    playBtn.classList.add('playing');
    playBtn.setAttribute('aria-label', 'Pause');
  } else {
    playIcon.innerHTML = PLAY_SVG;
    playBtn.classList.remove('playing');
    playBtn.setAttribute('aria-label', 'Play');
  }
}

function stopInterval() {
  clearInterval(intervalId);
  intervalId = null;
}

function startInterval() {
  intervalId = setInterval(() => {
    const advanced = player.next();
    if (!advanced) {
      player.pause();
      stopInterval();
    }
    render();
  }, 2000);
}

prevBtn.addEventListener('click', () => {
  player.previous();
  render();
});

nextBtn.addEventListener('click', () => {
  player.next();
  render();
});

playBtn.addEventListener('click', () => {
  player.togglePlay();
  if (player.playing) {
    startInterval();
  } else {
    stopInterval();
  }
  render();
});

document.addEventListener('keydown', (e) => {
  if (e.key === 'ArrowLeft') prevBtn.click();
  else if (e.key === 'ArrowRight') nextBtn.click();
  else if (e.key === ' ') { e.preventDefault(); playBtn.click(); }
});

async function init() {
  const response = await fetch('lyrics.txt');
  const text = await response.text();
  const lines = text.split('\n').filter(line => line.trim() !== '');
  player = new LyricsPlayer(lines);
  render();
}

init();

export class LyricsPlayer {
  constructor(lines) {
    this.lines = lines;
    this.currentIndex = 0;
    this.playing = false;
  }

  get currentLine() {
    return this.lines[this.currentIndex];
  }

  get totalLines() {
    return this.lines.length;
  }

  get progress() {
    return (this.currentIndex + 1) / this.lines.length;
  }

  get isAtStart() {
    return this.currentIndex === 0;
  }

  get isAtEnd() {
    return this.currentIndex === this.lines.length - 1;
  }

  next() {
    if (this.isAtEnd) return false;
    this.currentIndex++;
    return true;
  }

  previous() {
    if (this.isAtStart) return false;
    this.currentIndex--;
    return true;
  }

  play() {
    if (this.isAtEnd) this.currentIndex = 0;
    this.playing = true;
  }

  pause() {
    this.playing = false;
  }

  togglePlay() {
    this.playing ? this.pause() : this.play();
  }

  reset() {
    this.currentIndex = 0;
    this.playing = false;
  }
}

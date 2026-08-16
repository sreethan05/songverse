// src/audioPlayer.js - Full Web Audio & HTML5 Audio Engine with Analyser

import { store } from './store.js';

class AudioPlayer {
  constructor() {
    this.audio = new Audio();
    this.audio.crossOrigin = 'anonymous';
    this.audio.preload = 'auto';

    this.currentSong = null;
    this.queue = [];
    this.queueIndex = -1;
    this.isPlaying = false;
    this.currentTime = 0;
    this.duration = 0;
    this.isShuffle = false;
    this.isRepeat = false;
    this.listeners = new Set();

    // Audio Context & Analyser
    this.audioCtx = null;
    this.analyser = null;
    this.sourceNode = null;
    this.dataArray = new Uint8Array(64);

    this._setupAudioEvents();
  }

  _initAudioContext() {
    if (this.audioCtx) return;
    try {
      const AudioCtxClass = window.AudioContext || window.webkitAudioContext;
      if (AudioCtxClass) {
        this.audioCtx = new AudioCtxClass();
        this.analyser = this.audioCtx.createAnalyser();
        this.analyser.fftSize = 128;
        this.analyser.smoothingTimeConstant = 0.8;
        this.dataArray = new Uint8Array(this.analyser.frequencyBinCount);

        try {
          this.sourceNode = this.audioCtx.createMediaElementSource(this.audio);
          this.sourceNode.connect(this.analyser);
          this.analyser.connect(this.audioCtx.destination);
        } catch {
          // If already connected or CORS-protected audio source
        }
      }
    } catch (e) {
      console.warn('Web Audio API not initialized:', e);
    }
  }

  _setupAudioEvents() {
    this.audio.volume = store.state.volume;

    this.audio.addEventListener('timeupdate', () => {
      this.currentTime = this.audio.currentTime;
      this.duration = this.audio.duration || (this.currentSong ? this.currentSong.durationSeconds : 0);
      this._notify();
    });

    this.audio.addEventListener('play', () => {
      this.isPlaying = true;
      this._notify();
    });

    this.audio.addEventListener('pause', () => {
      this.isPlaying = false;
      this._notify();
    });

    this.audio.addEventListener('ended', () => {
      if (this.isRepeat) {
        this.audio.currentTime = 0;
        this.audio.play();
      } else {
        this.next();
      }
    });

    this.audio.addEventListener('error', (e) => {
      console.warn('Audio playback error, attempting fallback progress timer:', e);
      // If direct audio format fails, start synthetic progress for demo
      this._startSyntheticProgress();
    });
  }

  _startSyntheticProgress() {
    if (this.syntheticTimer) clearInterval(this.syntheticTimer);
    this.isPlaying = true;
    this.syntheticTimer = setInterval(() => {
      if (!this.isPlaying) return;
      this.currentTime += 1;
      if (this.currentTime >= (this.currentSong?.durationSeconds || 180)) {
        this.currentTime = 0;
        if (!this.isRepeat) this.next();
      }
      this._notify();
    }, 1000);
  }

  _notify() {
    const payload = {
      currentSong: this.currentSong,
      isPlaying: this.isPlaying,
      currentTime: this.currentTime,
      duration: this.duration || (this.currentSong ? this.currentSong.durationSeconds : 180),
      progressPercent: this.duration > 0 ? (this.currentTime / this.duration) * 100 : 0,
      queue: this.queue,
      queueIndex: this.queueIndex,
      isShuffle: this.isShuffle,
      isRepeat: this.isRepeat
    };
    this.listeners.forEach(cb => cb(payload));
  }

  subscribe(cb) {
    this.listeners.add(cb);
    this._notify();
    return () => this.listeners.delete(cb);
  }

  play(song, queueContext = []) {
    this._initAudioContext();
    if (this.audioCtx && this.audioCtx.state === 'suspended') {
      this.audioCtx.resume();
    }

    if (this.syntheticTimer) clearInterval(this.syntheticTimer);

    if (queueContext.length > 0) {
      this.queue = [...queueContext];
      this.queueIndex = this.queue.findIndex(s => s.id === song.id || s.title === song.title);
      if (this.queueIndex === -1) {
        this.queue.unshift(song);
        this.queueIndex = 0;
      }
    } else if (!this.queue.some(s => s.id === song.id || s.title === song.title)) {
      this.queue.push(song);
      this.queueIndex = this.queue.length - 1;
    }

    this.currentSong = song;
    store.addToHistory(song);

    if (song.audioUrl) {
      this.audio.src = song.audioUrl;
      this.audio.currentTime = 0;
      this.audio.play().catch(err => {
        console.warn('Autoplay prevented or stream issue, falling back:', err);
        this._startSyntheticProgress();
      });
    } else {
      this._startSyntheticProgress();
    }

    this.isPlaying = true;
    this._notify();
  }

  togglePlayPause() {
    if (!this.currentSong && this.queue.length > 0) {
      this.play(this.queue[0], this.queue);
      return;
    }
    if (!this.currentSong) return;

    if (this.audioCtx && this.audioCtx.state === 'suspended') {
      this.audioCtx.resume();
    }

    if (this.isPlaying) {
      this.audio.pause();
      this.isPlaying = false;
    } else {
      this.audio.play().catch(() => this._startSyntheticProgress());
      this.isPlaying = true;
    }
    this._notify();
  }

  seek(seconds) {
    const max = this.duration || (this.currentSong?.durationSeconds || 180);
    const clamped = Math.max(0, Math.min(seconds, max));
    this.currentTime = clamped;
    this.audio.currentTime = clamped;
    this._notify();
  }

  seekPercent(percent) {
    const max = this.duration || (this.currentSong?.durationSeconds || 180);
    this.seek((percent / 100) * max);
  }

  next() {
    if (this.queue.length === 0) return;
    let nextIdx = this.queueIndex + 1;
    if (nextIdx >= this.queue.length) {
      nextIdx = 0;
    }
    this.queueIndex = nextIdx;
    this.play(this.queue[nextIdx], this.queue);
  }

  previous() {
    if (this.queue.length === 0) return;
    if (this.currentTime > 3) {
      this.seek(0);
      return;
    }
    let prevIdx = this.queueIndex - 1;
    if (prevIdx < 0) {
      prevIdx = this.queue.length - 1;
    }
    this.queueIndex = prevIdx;
    this.play(this.queue[prevIdx], this.queue);
  }

  toggleShuffle() {
    this.isShuffle = !this.isShuffle;
    if (this.isShuffle && this.queue.length > 1) {
      const current = this.currentSong;
      const rest = this.queue.filter(s => s.id !== current?.id);
      for (let i = rest.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [rest[i], rest[j]] = [rest[j], rest[i]];
      }
      this.queue = current ? [current, ...rest] : rest;
      this.queueIndex = 0;
    }
    this._notify();
  }

  toggleRepeat() {
    this.isRepeat = !this.isRepeat;
    this._notify();
  }

  setVolume(fraction) {
    const clamped = Math.max(0, Math.min(1, fraction));
    this.audio.volume = clamped;
    store.setVolume(clamped);
    this._notify();
  }

  getFrequencyData() {
    if (this.analyser && this.isPlaying) {
      try {
        this.analyser.getByteFrequencyData(this.dataArray);
        // If dataArray is completely zero (due to cross-origin media), generate dynamic harmonic waves
        let sum = 0;
        for (let i = 0; i < this.dataArray.length; i++) sum += this.dataArray[i];
        if (sum > 0) return this.dataArray;
      } catch {
        // Fallback
      }
    }

    // High quality synthetic audio spectrum generator for smooth visualizer responsiveness
    if (this.isPlaying) {
      const time = performance.now() / 1000;
      const bpm = this.currentSong?.tempoBpm || 120;
      const beat = (time * (bpm / 60)) % 1;
      const bassKick = Math.pow(Math.sin(beat * Math.PI), 4) * 180 + 50;

      for (let i = 0; i < 64; i++) {
        const wave = Math.sin(time * 3 + i * 0.2) * 40 + Math.cos(time * 5 + i * 0.4) * 30;
        const decay = Math.max(0, 1 - (i / 50));
        this.dataArray[i] = Math.min(255, Math.max(10, (bassKick * decay) + wave + 40));
      }
      return this.dataArray;
    }

    this.dataArray.fill(0);
    return this.dataArray;
  }
}

export const audioPlayer = new AudioPlayer();

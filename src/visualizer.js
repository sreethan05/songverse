// src/visualizer.js - 4 Audio Visualizer Engines (Aura, Vinyl, Cyberpunk, Studio)

import { audioPlayer } from './audioPlayer.js';
import { store, THEMES } from './store.js';

export class Visualizer {
  constructor(canvas) {
    this.canvas = canvas;
    this.ctx = canvas.getContext('2d');
    this.animationId = null;
    this.rotation = 0;
    this.particles = [];
    this.style = store.state.visualizerStyle || 'AURA';

    this._initParticles();
    this.resize();
    window.addEventListener('resize', () => this.resize());
  }

  setStyle(style) {
    this.style = style;
    store.setVisualizerStyle(style);
  }

  resize() {
    const rect = this.canvas.parentElement.getBoundingClientRect();
    const dpr = window.devicePixelRatio || 1;
    this.canvas.width = rect.width * dpr;
    this.canvas.height = rect.height * dpr;
    this.ctx.scale(dpr, dpr);
    this.width = rect.width;
    this.height = rect.height;
  }

  _initParticles() {
    this.particles = [];
    for (let i = 0; i < 40; i++) {
      this.particles.push({
        x: Math.random() * 400,
        y: Math.random() * 400,
        radius: Math.random() * 3 + 1,
        vx: (Math.random() - 0.5) * 1.5,
        vy: (Math.random() - 0.5) * 1.5,
        alpha: Math.random() * 0.5 + 0.2
      });
    }
  }

  start() {
    if (this.animationId) return;
    const render = () => {
      this.draw();
      this.animationId = requestAnimationFrame(render);
    };
    render();
  }

  stop() {
    if (this.animationId) {
      cancelAnimationFrame(this.animationId);
      this.animationId = null;
    }
  }

  draw() {
    const freq = audioPlayer.getFrequencyData();
    const isPlaying = audioPlayer.isPlaying;
    const currentTheme = THEMES[store.state.theme] || THEMES.CYBERPUNK;
    const accent = currentTheme.accent;
    const accentSec = currentTheme.accentSecondary;

    this.ctx.clearRect(0, 0, this.width, this.height);

    switch (this.style) {
      case 'VINYL':
        this._drawVinyl(freq, isPlaying, accent);
        break;
      case 'CYBERPUNK':
        this._drawCyberpunk(freq, isPlaying, accent, accentSec);
        break;
      case 'MINIMAL':
        this._drawMinimal(freq, isPlaying, accent);
        break;
      case 'AURA':
      default:
        this._drawAura(freq, isPlaying, accent, accentSec);
        break;
    }
  }

  // --- 1. AURA VISUALIZER (Fluid Ambient Energy) ---
  _drawAura(freq, isPlaying, accent, accentSec) {
    const cx = this.width / 2;
    const cy = this.height / 2;
    const bass = (freq[0] + freq[1] + freq[2]) / 3 || 0;
    const pulse = (bass / 255) * 40;

    // Glowing background orbs
    const grad = this.ctx.createRadialGradient(cx, cy, 20, cx, cy, Math.max(cx, cy) * 0.9);
    grad.addColorStop(0, accent + (isPlaying ? '40' : '15'));
    grad.addColorStop(0.5, accentSec + (isPlaying ? '25' : '0A'));
    grad.addColorStop(1, 'transparent');

    this.ctx.fillStyle = grad;
    this.ctx.fillRect(0, 0, this.width, this.height);

    // Dynamic frequency halo rings
    const numPoints = 64;
    const baseRadius = Math.min(cx, cy) * 0.48 + pulse;

    this.ctx.save();
    this.ctx.translate(cx, cy);

    if (isPlaying) {
      this.rotation += 0.008;
    }
    this.ctx.rotate(this.rotation);

    // Outer reactive glow wave
    this.ctx.beginPath();
    for (let i = 0; i < numPoints; i++) {
      const angle = (i / numPoints) * Math.PI * 2;
      const fVal = (freq[i % freq.length] / 255) * 35;
      const r = baseRadius + fVal;
      const x = Math.cos(angle) * r;
      const y = Math.sin(angle) * r;
      if (i === 0) this.ctx.moveTo(x, y);
      else this.ctx.lineTo(x, y);
    }
    this.ctx.closePath();
    this.ctx.strokeStyle = accent;
    this.ctx.lineWidth = 3;
    this.ctx.shadowColor = accent;
    this.ctx.shadowBlur = 20;
    this.ctx.stroke();

    // Inner counter-rotating ring
    this.ctx.beginPath();
    for (let i = 0; i < numPoints; i++) {
      const angle = (i / numPoints) * Math.PI * 2;
      const fVal = (freq[(numPoints - i) % freq.length] / 255) * 20;
      const r = (baseRadius * 0.75) + fVal;
      const x = Math.cos(-angle) * r;
      const y = Math.sin(-angle) * r;
      if (i === 0) this.ctx.moveTo(x, y);
      else this.ctx.lineTo(x, y);
    }
    this.ctx.closePath();
    this.ctx.strokeStyle = accentSec;
    this.ctx.lineWidth = 2;
    this.ctx.shadowColor = accentSec;
    this.ctx.shadowBlur = 15;
    this.ctx.stroke();

    this.ctx.restore();
  }

  // --- 2. VINYL VISUALIZER (Realistic Rotating Record) ---
  _drawVinyl(freq, isPlaying, accent) {
    const cx = this.width / 2;
    const cy = this.height / 2;
    const radius = Math.min(cx, cy) * 0.75;
    const bass = (freq[0] + freq[1]) / 2 || 0;

    if (isPlaying) {
      this.rotation += 0.025;
    }

    this.ctx.save();
    this.ctx.translate(cx, cy);
    this.ctx.rotate(this.rotation);

    // 1. Vinyl Black Disc
    this.ctx.beginPath();
    this.ctx.arc(0, 0, radius, 0, Math.PI * 2);
    this.ctx.fillStyle = '#0d0d11';
    this.ctx.shadowColor = '#000000';
    this.ctx.shadowBlur = 30;
    this.ctx.fill();

    // 2. Vinyl Grooves
    this.ctx.shadowBlur = 0;
    this.ctx.strokeStyle = 'rgba(255, 255, 255, 0.05)';
    this.ctx.lineWidth = 1;
    for (let r = radius * 0.45; r < radius * 0.95; r += 7) {
      this.ctx.beginPath();
      this.ctx.arc(0, 0, r, 0, Math.PI * 2);
      this.ctx.stroke();
    }

    // 3. Realistic Sheen Reflection (Specular lighting)
    const sheen = this.ctx.createLinearGradient(-radius, -radius, radius, radius);
    sheen.addColorStop(0, 'rgba(255, 255, 255, 0.12)');
    sheen.addColorStop(0.48, 'transparent');
    sheen.addColorStop(0.52, 'rgba(255, 255, 255, 0.08)');
    sheen.addColorStop(1, 'transparent');
    this.ctx.fillStyle = sheen;
    this.ctx.beginPath();
    this.ctx.arc(0, 0, radius, 0, Math.PI * 2);
    this.ctx.fill();

    // 4. Center Label & Album Art Ring
    const labelRadius = radius * 0.38;
    this.ctx.beginPath();
    this.ctx.arc(0, 0, labelRadius, 0, Math.PI * 2);
    this.ctx.fillStyle = accent;
    this.ctx.fill();

    // 5. Center Spindle Hole
    this.ctx.beginPath();
    this.ctx.arc(0, 0, labelRadius * 0.2, 0, Math.PI * 2);
    this.ctx.fillStyle = '#111116';
    this.ctx.fill();
    this.ctx.strokeStyle = 'rgba(255, 255, 255, 0.3)';
    this.ctx.stroke();

    this.ctx.restore();
  }

  // --- 3. CYBERPUNK EQUALIZER BARS ---
  _drawCyberpunk(freq, isPlaying, accent, accentSec) {
    const numBars = 32;
    const barWidth = (this.width / numBars) * 0.7;
    const gap = (this.width / numBars) * 0.3;
    const startX = gap / 2;

    for (let i = 0; i < numBars; i++) {
      const fVal = freq[i * 2] || 0;
      const heightFrac = isPlaying ? (fVal / 255) : 0.08;
      const barHeight = Math.max(6, heightFrac * (this.height * 0.75));
      const x = startX + i * (barWidth + gap);
      const y = this.height - barHeight - 20;

      const grad = this.ctx.createLinearGradient(x, y, x, this.height);
      grad.addColorStop(0, accent);
      grad.addColorStop(0.6, accentSec);
      grad.addColorStop(1, 'transparent');

      this.ctx.fillStyle = grad;
      this.ctx.shadowColor = accent;
      this.ctx.shadowBlur = isPlaying ? 14 : 4;

      // Rounded top bar
      this.ctx.beginPath();
      this.ctx.roundRect(x, y, barWidth, barHeight, [4, 4, 0, 0]);
      this.ctx.fill();

      // Peak neon indicator cap
      this.ctx.fillStyle = '#FFFFFF';
      this.ctx.fillRect(x, y - 4, barWidth, 2);
    }
  }

  // --- 4. STUDIO MINIMAL OSCILLOSCOPE WAVE ---
  _drawMinimal(freq, isPlaying, accent) {
    const cy = this.height / 2;
    this.ctx.beginPath();
    this.ctx.moveTo(0, cy);

    const step = this.width / 64;
    for (let i = 0; i < 64; i++) {
      const fVal = (freq[i] / 255) || 0;
      const amp = isPlaying ? fVal * (this.height * 0.38) : Math.sin(i * 0.3) * 6;
      const x = i * step;
      const y = cy + (i % 2 === 0 ? amp : -amp);
      this.ctx.lineTo(x, y);
    }

    this.ctx.strokeStyle = accent;
    this.ctx.lineWidth = 3;
    this.ctx.shadowColor = accent;
    this.ctx.shadowBlur = 12;
    this.ctx.stroke();

    // Mirror soft baseline
    this.ctx.beginPath();
    this.ctx.moveTo(0, cy);
    this.ctx.lineTo(this.width, cy);
    this.ctx.strokeStyle = 'rgba(255, 255, 255, 0.1)';
    this.ctx.lineWidth = 1;
    this.ctx.shadowBlur = 0;
    this.ctx.stroke();
  }
}

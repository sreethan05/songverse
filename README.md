# 🎵 SongVerse

<div align="center">
  <h3>Next-Generation AI-Powered Music Player & Discovery App</h3>
  <p>Available as a Native Android App (Jetpack Compose + Material 3) & Interactive Web Experience</p>
</div>

---

## ✨ Features

- **⚡ Ultra-Fast Search Engine**: Sub-150ms parallel queries across Global and Indian music catalogs (Bollywood, Punjabi, Pop, Hip-Hop, Indie, Synthwave, Rock, and more).
- **🎶 Instant Audio Streaming & Previews**: Stream high-quality audio previews with animated waveform visualizers and progress controls.
- **🖼️ Hi-Res 600x600 Album Artwork**: Dynamic glowing vinyl covers, rotating records, and album art style toggles.
- **🎤 Synchronized Lyrics & AI Meaning**: Line-by-line synced lyrics with Gemini AI-powered lyric breakdowns, emotional tone analysis, and trivia.
- **📚 Rich Local & Cloud Library**: 
  - Room Database persistence for Favorites, Custom Playlists, and Playback History.
  - Preloaded curated chartbusters ready to play offline.
- **🎯 1-Tap Discovery**: Quick-access chips for top artists (*Arijit Singh, Diljit Dosanjh, Sidhu Moose Wala, Taylor Swift, Karan Aujla, The Weeknd, AP Dhillon, Coldplay, Badshah*) and genres (*Bollywood Hits, Punjabi Bangers, Global Top 50, Lo-Fi Chill*).
- **🎨 Glassmorphism & Cyberpunk Neon UI**: Full Material 3 theming with dynamic dark themes, cyan/magenta neon accents, and smooth transitions.

---

## 📱 Android App Setup

### Prerequisites
- **Android Studio** Ladybug or newer
- **JDK 17 or JDK 21**
- **Android SDK** API 26+ (Target SDK: 35)

### Build & Run on Device
1. Open this repository in Android Studio.
2. Connect your Android device via USB with **USB Debugging** enabled.
3. Run the Gradle build:
   ```bash
   ./gradlew assembleDebug
   ```
4. Or use the automated install script:
   ```powershell
   powershell -ExecutionPolicy Bypass -File .\run_build_and_install.ps1
   ```

---

## 🌐 Web App Setup

### Prerequisites
- **Node.js** v18+

### Run Locally
```bash
npm install
npm run dev
```

---

## 🔑 Environment Variables
Create a `.env` file in the root directory (see `.env.example`):
```env
GEMINI_API_KEY=your_gemini_api_key_here
```

---

## 🛠️ Tech Stack
- **Android Native**: Kotlin, Jetpack Compose, Material 3, AndroidX Media3 / MediaPlayer, Room DB, KSP, Coroutines, Flow.
- **Web App**: Vite, Vanilla JavaScript, HTML5 Web Audio API, Canvas 2D Visualizer, Modern CSS.
- **APIs**: Apple iTunes Search API, Google Gemini API.

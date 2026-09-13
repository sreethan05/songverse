# 🎵 SongVerse

<div align="center">

### 🎧 Your Music. Your Vibe. Your Verse.

**An AI-powered music discovery and playback experience built for modern listeners.**

Native Android • Interactive Web Experience • AI Lyrics • Smart Discovery • Personalized Library

<br/>

![Android](https://img.shields.io/badge/Android-Kotlin%20%7C%20Jetpack%20Compose-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Web](https://img.shields.io/badge/Web-Vite%20%7C%20JavaScript-646CFF?style=for-the-badge&logo=vite&logoColor=white)
![AI](https://img.shields.io/badge/AI-Google%20Gemini-4285F4?style=for-the-badge&logo=google&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.x-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)

</div>

---

## 🌌 About SongVerse

**SongVerse** is a next-generation music discovery and playback platform designed to make listening to music more immersive, intelligent, and personal.

It combines a **native Android application** with an **interactive web experience**, bringing together music discovery, audio previews, synchronized lyrics, AI-powered lyric interpretation, playlists, favorites, playback history, and an immersive visual interface.

Instead of simply playing a song, SongVerse aims to help you **discover the music, understand the lyrics, and experience the vibe.**

---

## ✨ Key Features

### ⚡ Fast Music Discovery

Search and discover music across global and Indian catalogs with support for:

- 🇮🇳 Bollywood
- 🎤 Punjabi
- 🎸 Rock
- 🎧 Hip-Hop
- 🌙 Lo-Fi
- 🎹 Indie
- 🌌 Synthwave
- 🌎 Global Pop

SongVerse is designed around fast parallel search and responsive discovery.

### 🎶 Audio Streaming & Previews

Enjoy music previews with:

- High-quality audio playback
- Playback progress controls
- Animated waveform visualization
- Play / pause controls
- Song duration tracking
- Smooth playback experience

### 🎨 Immersive Music Visualizer

SongVerse turns music playback into a visual experience with:

- Dynamic waveform visualizers
- Animated album artwork
- Glowing vinyl-style records
- Smooth transitions
- Reactive visual elements
- Cyberpunk-inspired aesthetics

### 🖼️ High-Resolution Album Artwork

Dynamic artwork features include:

- 600×600 album artwork
- Glowing album covers
- Rotating vinyl records
- Artwork style variations

### 🎤 Synchronized Lyrics

Follow lyrics while the song plays with synchronized, line-by-line lyric presentation.

### 🤖 AI-Powered Lyric Intelligence

Using **Google Gemini**, SongVerse can provide:

- 📝 Lyric explanations
- 💭 Meaning behind lyrics
- ❤️ Emotional tone analysis
- 🧠 Context and interpretation
- 🎯 Interesting song trivia

This turns a traditional lyrics screen into an interactive music-learning experience.

### 📚 Personal Music Library

Keep track of:

- ❤️ Favorites
- 🎵 Custom playlists
- 🕘 Playback history

On Android, local persistence is powered by **Room Database**.

### 🎯 One-Tap Discovery

Quick discovery shortcuts make it easy to explore popular artists and genres.

**Artists:** Arijit Singh, Diljit Dosanjh, Sidhu Moose Wala, Taylor Swift, Karan Aujla, The Weeknd, AP Dhillon, Coldplay, Badshah.

**Categories:** Bollywood Hits, Punjabi Bangers, Global Top 50, Lo-Fi Chill.

---

## 🖥️ Platforms

### 📱 Native Android App

Built with modern Android technologies including Jetpack Compose, Material 3, AndroidX Media3 / MediaPlayer, Room, Coroutines, Flow, and KSP.

### 🌐 Interactive Web Experience

A lightweight web experience built with Vite, Vanilla JavaScript, HTML5 Audio, Web Audio API, Canvas 2D, and modern CSS.

---

## 🏗️ Technology Stack

| Area | Technologies |
|---|---|
| **Android** | Kotlin, Jetpack Compose, Material 3, AndroidX Media3 / MediaPlayer |
| **Database** | Room Database, KSP |
| **Async / Reactive** | Coroutines, Flow |
| **Web** | Vite, Vanilla JavaScript, HTML5, CSS |
| **Audio** | Web Audio API, Android Media3 / MediaPlayer |
| **Visualization** | Canvas 2D, Waveform Visualizer |
| **AI** | Google Gemini API |
| **Music API** | Apple iTunes Search API |

---

## 🎨 Design Philosophy

SongVerse follows a **Glassmorphism × Cyberpunk Neon** visual direction.

The interface focuses on:

- 🌑 Deep dark backgrounds
- 💠 Cyan and magenta neon accents
- 🪟 Glass-like surfaces
- ✨ Glow effects
- 🎞️ Smooth animations
- 💿 Music-focused visual elements
- 📱 Responsive layouts

The goal is to make music feel like an **interactive experience**, not just playback.

---

# 🚀 Getting Started

## 📱 Android Setup

### Requirements

- Android Studio **Ladybug or newer**
- JDK **17 or 21**
- Android SDK **API 26+**
- Target SDK **35**
- Physical Android device or emulator

### Clone the repository

```bash
git clone https://github.com/sreethan05/songverse.git
cd songverse
```

### Open in Android Studio

Open the project in Android Studio and allow Gradle to synchronize.

### Build

```bash
./gradlew assembleDebug
```

### Install on a connected device

Connect an Android device with USB debugging enabled and run the application from Android Studio.

You can also use the provided PowerShell script:

```powershell
powershell -ExecutionPolicy Bypass -File .\run_build_and_install.ps1
```

---

## 🌐 Web Setup

### Requirements

- Node.js **18+**
- npm

### Install dependencies

```bash
npm install
```

### Start the development server

```bash
npm run dev
```

Vite will display the local development URL in the terminal.

---

## 🔐 Environment Variables

Create a `.env` file in the project root:

```env
GEMINI_API_KEY=your_gemini_api_key_here
```

> ⚠️ **Never commit real API keys or secrets to GitHub.**

Use `.env.example` as the configuration reference when available.

---

## 🔄 How SongVerse Works

```text
                    ┌───────────────────┐
                    │     SONGVERSE     │
                    └─────────┬─────────┘
                              │
             ┌────────────────┴────────────────┐
             │                                 │
       ┌─────▼─────┐                     ┌─────▼─────┐
       │   Android │                     │    Web    │
       │    App    │                     │ Experience│
       └─────┬─────┘                     └─────┬─────┘
             │                                 │
             └────────────────┬────────────────┘
                              │
                     ┌────────▼────────┐
                     │ Music Discovery │
                     └────────┬────────┘
                              │
                     ┌────────▼────────┐
                     │ Audio Playback  │
                     └────────┬────────┘
                              │
              ┌───────────────┼────────────────┐
              │               │                │
        ┌─────▼─────┐   ┌─────▼─────┐   ┌────▼────┐
        │  Lyrics   │   │ Visualizer │   │ Library │
        └─────┬─────┘   └───────────┘   └────┬─────┘
              │                                │
        ┌─────▼─────┐                    ┌─────▼─────┐
        │ Gemini AI │                    │  Room DB  │
        │  Analysis │                    │ Favorites │
        └───────────┘                    │ Playlists │
                                         │  History  │
                                         └───────────┘
```

---

## 🧠 AI Music Experience

A typical SongVerse experience:

```text
🎵 Select Song
      ↓
🎧 Start Playback
      ↓
🎤 View Synchronized Lyrics
      ↓
🤖 Analyze Lyrics with Gemini AI
      ↓
💭 Understand Meaning & Emotion
      ↓
✨ Discover More About the Song
```

---

## 📂 Project Overview

```text
SongVerse/
│
├── 📱 Android Application
│   ├── Kotlin
│   ├── Jetpack Compose
│   ├── Material 3
│   ├── Media3 / MediaPlayer
│   ├── Room Database
│   └── Coroutines / Flow
│
├── 🌐 Web Experience
│   ├── Vite
│   ├── JavaScript
│   ├── HTML
│   ├── CSS
│   ├── Web Audio API
│   └── Canvas Visualizer
│
├── 🤖 AI Integration
│   └── Google Gemini
│
├── 🎵 Music Discovery
│   └── Apple iTunes Search API
│
└── 📄 Configuration & Documentation
```

---

## 🔮 Future Roadmap

Potential future improvements include:

- 🎧 Full-length music streaming
- 📥 Offline song downloads
- 🎙️ Voice-controlled music search
- 🧠 Personalized AI recommendations
- 🎼 AI-generated playlists
- 🎤 Improved lyric synchronization
- 📊 Listening statistics
- 👥 Social playlists
- 🔥 Trending music dashboard
- 🌎 Additional music providers
- 🖥️ Desktop application
- 🔄 Cross-device synchronization

---

## 🔒 Security & Privacy

When developing or deploying SongVerse:

- Never expose API keys in source code.
- Keep `.env` files out of version control.
- Never commit private credentials.
- Use environment-specific configuration.
- Review third-party API usage and privacy policies.

---

## 🤝 Contributing

Contributions, ideas, improvements, and bug reports are welcome.

### 1. Create a feature branch

```bash
git checkout -b feature/amazing-feature
```

### 2. Make your changes

Implement your feature or fix.

### 3. Commit your changes

```bash
git add .
git commit -m "feat: add amazing feature"
```

### 4. Push your branch

```bash
git push origin feature/amazing-feature
```

### 5. Open a Pull Request

Describe your changes clearly and explain why they improve SongVerse.

---

## 🐛 Issues & Feedback

Found a bug or have an idea? Open an issue and include:

- What happened
- What you expected
- Steps to reproduce
- Device or browser information
- Screenshots or logs when useful

---

## 📜 License

This project is currently provided for educational and personal development purposes.

See the repository for the latest licensing information.

---

<div align="center">

## 🎵 SongVerse

### Discover. Listen. Understand. Experience.

**Built with Kotlin • Jetpack Compose • Vite • Gemini AI • Music APIs**

⭐ If you like the project, consider giving it a star!

</div>

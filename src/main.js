// src/main.js - SongVerse Application Controller

import { store, THEMES } from './store.js';
import { audioPlayer } from './audioPlayer.js';
import { searchSongs, PRELOADED_SONGS, GENRES } from './musicApi.js';
import { Visualizer } from './visualizer.js';

let visualizer = null;
let currentSearchResults = [...PRELOADED_SONGS];
let activeTab = 'discover';

document.addEventListener('DOMContentLoaded', () => {
  // 1. Initialize Icons
  if (window.lucide) window.lucide.createIcons();

  // 2. Initialize Visualizer
  const canvas = document.getElementById('visualizer-canvas');
  if (canvas) {
    visualizer = new Visualizer(canvas);
    visualizer.start();
  }

  // 3. Render Initial State
  applyTheme(store.state.theme);
  renderTrendingSongs();
  renderGenres();
  renderRecentSearchChips();
  renderLibrary();

  // 4. Auto-load initial preloaded song
  if (PRELOADED_SONGS.length > 0) {
    audioPlayer.currentSong = PRELOADED_SONGS[0];
    audioPlayer.queue = [...PRELOADED_SONGS];
    audioPlayer.queueIndex = 0;
    updatePlayerUI(audioPlayer.currentSong, false);
  }

  // 5. Setup Event Listeners
  setupNavigation();
  setupSearch();
  setupPlayerControls();
  setupModals();
  setupVisualizerPills();
  setupKeyboardShortcuts();

  // 6. Subscribe to Audio & Store Changes
  audioPlayer.subscribe(handlePlayerUpdate);
  store.subscribe(handleStoreUpdate);
});

// --- Theme Management ---
function applyTheme(themeKey) {
  const theme = THEMES[themeKey] || THEMES.CYBERPUNK;
  document.body.className = `theme-${theme.id}`;
}

// --- Navigation Tabs ---
function setupNavigation() {
  const tabBtns = document.querySelectorAll('.nav-tab-btn');
  tabBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      const targetTab = btn.dataset.tab;
      switchTab(targetTab);
    });
  });

  document.getElementById('brand-logo-btn')?.addEventListener('click', () => {
    switchTab('discover');
  });
}

function switchTab(tabName) {
  activeTab = tabName;
  document.querySelectorAll('.nav-tab-btn').forEach(btn => {
    btn.classList.toggle('active', btn.dataset.tab === tabName);
  });
  document.querySelectorAll('.view-section').forEach(sec => {
    sec.classList.toggle('active', sec.id === `view-${tabName}`);
  });
  if (window.lucide) window.lucide.createIcons();
}

// --- Search Engine ---
let searchDebounceTimer = null;
function setupSearch() {
  const searchInput = document.getElementById('global-search-input');
  const clearBtn = document.getElementById('search-clear-btn');

  searchInput.addEventListener('input', (e) => {
    const q = e.target.value;
    clearBtn.classList.toggle('visible', q.length > 0);

    clearTimeout(searchDebounceTimer);
    searchDebounceTimer = setTimeout(() => {
      performSearch(q);
    }, 280);
  });

  searchInput.addEventListener('focus', () => {
    if (activeTab !== 'search') switchTab('search');
  });

  searchInput.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && searchInput.value.trim()) {
      store.addRecentSearch(searchInput.value.trim());
      renderRecentSearchChips();
    }
  });

  clearBtn.addEventListener('click', () => {
    searchInput.value = '';
    clearBtn.classList.remove('visible');
    performSearch('');
  });

  document.getElementById('clear-recents-btn')?.addEventListener('click', () => {
    store.clearRecentSearches();
    renderRecentSearchChips();
  });
}

async function performSearch(query) {
  const listContainer = document.getElementById('search-list-container');
  const title = document.getElementById('search-results-title');
  const count = document.getElementById('search-count');

  if (!query.trim()) {
    title.textContent = 'Catalog Tracks';
    currentSearchResults = [...PRELOADED_SONGS];
    renderSongList(listContainer, currentSearchResults);
    count.textContent = `${currentSearchResults.length} tracks`;
    return;
  }

  title.textContent = `Results for "${query}"`;
  listContainer.innerHTML = `<div style="padding: 30px; text-align: center; color: var(--color-text-secondary);"><i data-lucide="loader-2" class="spin"></i> Searching worldwide music...</div>`;
  if (window.lucide) window.lucide.createIcons();

  const results = await searchSongs(query);
  currentSearchResults = results;
  count.textContent = `${results.length} songs found`;
  renderSongList(listContainer, results);
}

function renderRecentSearchChips() {
  const container = document.getElementById('recent-chips');
  if (!container) return;
  const searches = store.state.recentSearches;
  if (searches.length === 0) {
    container.innerHTML = `<span style="font-size: 0.8rem; color: var(--color-text-muted);">No recent searches</span>`;
    return;
  }

  container.innerHTML = searches.map(q => `
    <button class="nav-tab-btn" style="background: rgba(255,255,255,0.06); padding: 5px 12px; font-size: 0.8rem;" data-search-chip="${q}">
      ${q}
    </button>
  `).join('');

  container.querySelectorAll('[data-search-chip]').forEach(btn => {
    btn.addEventListener('click', () => {
      const q = btn.dataset.searchChip;
      const input = document.getElementById('global-search-input');
      input.value = q;
      document.getElementById('search-clear-btn')?.classList.add('visible');
      switchTab('search');
      performSearch(q);
    });
  });
}

// --- Render Discover View ---
function renderTrendingSongs() {
  const container = document.getElementById('trending-grid');
  if (!container) return;

  container.innerHTML = PRELOADED_SONGS.map(song => createSongCardHtml(song)).join('');
  attachCardEvents(container, PRELOADED_SONGS);
  if (window.lucide) window.lucide.createIcons();
}

function renderGenres() {
  const container = document.getElementById('genres-grid');
  if (!container) return;

  container.innerHTML = GENRES.map(g => `
    <div class="genre-card" style="background: linear-gradient(135deg, ${g.color}33, ${g.color}11); border-color: ${g.color}44;" data-genre-query="${g.query}">
      <div class="genre-name">${g.name}</div>
      <div class="genre-sub">Explore Hits</div>
    </div>
  `).join('');

  container.querySelectorAll('[data-genre-query]').forEach(card => {
    card.addEventListener('click', () => {
      const q = card.dataset.genreQuery;
      const input = document.getElementById('global-search-input');
      input.value = q;
      document.getElementById('search-clear-btn')?.classList.add('visible');
      switchTab('search');
      performSearch(q);
    });
  });
}

function createSongCardHtml(song) {
  const isFav = store.isFavorite(song);
  const isCur = audioPlayer.currentSong?.title === song.title;
  return `
    <div class="song-card ${isCur ? 'playing' : ''}" data-song-id="${song.id || song.title}">
      <div class="card-art-wrap">
        <img class="card-art-img" src="${song.coverUrl}" alt="${song.title}" loading="lazy">
        <div class="card-play-overlay">
          <button class="card-play-btn" data-action="play">
            <i data-lucide="${isCur && audioPlayer.isPlaying ? 'pause' : 'play'}"></i>
          </button>
        </div>
      </div>
      <div class="card-title">${song.title}</div>
      <div class="card-artist">${song.artist}</div>
      <div class="card-meta">
        <span>${song.genre || 'Music'}</span>
        <div class="card-actions">
          <button class="card-heart-btn ${isFav ? 'active' : ''}" data-action="like" title="Favorite">
            <i data-lucide="heart"></i>
          </button>
        </div>
      </div>
    </div>
  `;
}

function attachCardEvents(container, songsList) {
  container.querySelectorAll('.song-card').forEach(card => {
    const id = card.dataset.songId;
    const song = songsList.find(s => (s.id || s.title) === id);
    if (!song) return;

    card.addEventListener('click', (e) => {
      if (e.target.closest('[data-action="like"]')) {
        store.toggleFavorite(song);
        renderLibrary();
        return;
      }
      audioPlayer.play(song, songsList);
    });
  });
}

// --- Render Song Lists (Table View) ---
function renderSongList(container, songsList) {
  if (!songsList || songsList.length === 0) {
    container.innerHTML = `<div style="padding: 40px; text-align: center; color: var(--color-text-muted);">No songs found. Try a different search!</div>`;
    return;
  }

  container.innerHTML = songsList.map((song, i) => {
    const isFav = store.isFavorite(song);
    const isCur = audioPlayer.currentSong?.title === song.title;
    const mins = Math.floor(song.durationSeconds / 60);
    const secs = String(song.durationSeconds % 60).padStart(2, '0');

    return `
      <div class="song-list-row ${isCur ? 'playing' : ''}" data-song-id="${song.id || song.title}">
        <img class="list-row-art" src="${song.coverUrl}" alt="${song.title}">
        <div class="list-row-info">
          <div class="list-row-title">${song.title}</div>
          <div class="list-row-artist">${song.artist}</div>
        </div>
        <div class="list-row-album">${song.album || 'Single'}</div>
        <div class="list-row-duration">${mins}:${secs}</div>
        <div class="list-row-actions">
          <button class="card-heart-btn ${isFav ? 'active' : ''}" data-action="like" title="Favorite">
            <i data-lucide="heart"></i>
          </button>
        </div>
      </div>
    `;
  }).join('');

  container.querySelectorAll('.song-list-row').forEach(row => {
    const id = row.dataset.songId;
    const song = songsList.find(s => (s.id || s.title) === id);
    if (!song) return;

    row.addEventListener('click', (e) => {
      if (e.target.closest('[data-action="like"]')) {
        store.toggleFavorite(song);
        renderLibrary();
        return;
      }
      audioPlayer.play(song, songsList);
    });
  });

  if (window.lucide) window.lucide.createIcons();
}

// --- Render Library View ---
function renderLibrary() {
  const favs = store.state.favorites;
  const favContainer = document.getElementById('favorites-list-container');
  const favCount = document.getElementById('favorites-count');
  if (favCount) favCount.textContent = `${favs.length} songs`;
  if (favContainer) renderSongList(favContainer, favs);

  // Playlists
  const plGrid = document.getElementById('playlists-grid');
  if (plGrid) {
    plGrid.innerHTML = store.state.playlists.map(pl => `
      <div class="song-card" data-playlist-id="${pl.id}">
        <div class="card-art-wrap" style="background: linear-gradient(135deg, rgba(var(--color-accent-rgb),0.2), rgba(var(--color-accent-sec-rgb),0.2)); display: flex; align-items: center; justify-content: center;">
          <i data-lucide="music" style="width: 48px; height: 48px; color: var(--color-accent);"></i>
        </div>
        <div class="card-title">${pl.name}</div>
        <div class="card-artist">${pl.songs.length} songs</div>
        <div class="card-meta">
          <span>${pl.description || 'Custom Playlist'}</span>
        </div>
      </div>
    `).join('');

    plGrid.querySelectorAll('[data-playlist-id]').forEach(card => {
      card.addEventListener('click', () => {
        const id = card.dataset.playlistId;
        const pl = store.state.playlists.find(p => p.id === id);
        if (pl && pl.songs.length > 0) {
          audioPlayer.play(pl.songs[0], pl.songs);
        }
      });
    });
  }

  // History
  const historyContainer = document.getElementById('history-list-container');
  if (historyContainer) {
    renderSongList(historyContainer, store.state.history);
  }

  if (window.lucide) window.lucide.createIcons();
}

// --- Player Controls Setup ---
function setupPlayerControls() {
  const playBtn = document.getElementById('ctrl-play');
  const prevBtn = document.getElementById('ctrl-prev');
  const nextBtn = document.getElementById('ctrl-next');
  const shuffleBtn = document.getElementById('ctrl-shuffle');
  const repeatBtn = document.getElementById('ctrl-repeat');
  const heartBtn = document.getElementById('player-heart-btn');
  const modalHeartBtn = document.getElementById('modal-heart-btn');
  const seekBar = document.getElementById('seek-bar-container');
  const volumeSlider = document.getElementById('volume-slider');

  playBtn?.addEventListener('click', () => audioPlayer.togglePlayPause());
  prevBtn?.addEventListener('click', () => audioPlayer.previous());
  nextBtn?.addEventListener('click', () => audioPlayer.next());
  shuffleBtn?.addEventListener('click', () => {
    audioPlayer.toggleShuffle();
    shuffleBtn.classList.toggle('active', audioPlayer.isShuffle);
  });
  repeatBtn?.addEventListener('click', () => {
    audioPlayer.toggleRepeat();
    repeatBtn.classList.toggle('active', audioPlayer.isRepeat);
  });

  const toggleHeart = () => {
    if (audioPlayer.currentSong) {
      store.toggleFavorite(audioPlayer.currentSong);
      const isFav = store.isFavorite(audioPlayer.currentSong);
      heartBtn?.classList.toggle('active', isFav);
      modalHeartBtn?.classList.toggle('active', isFav);
      renderLibrary();
    }
  };

  heartBtn?.addEventListener('click', toggleHeart);
  modalHeartBtn?.addEventListener('click', toggleHeart);

  seekBar?.addEventListener('click', (e) => {
    const rect = seekBar.getBoundingClientRect();
    const percent = ((e.clientX - rect.left) / rect.width) * 100;
    audioPlayer.seekPercent(percent);
  });

  volumeSlider?.addEventListener('input', (e) => {
    audioPlayer.setVolume(parseFloat(e.target.value));
  });

  document.getElementById('clear-history-btn')?.addEventListener('click', () => {
    store.clearHistory();
    renderLibrary();
  });
}

// --- Handle Audio Player State Updates ---
let lastRenderedLyricsSongId = null;
function handlePlayerUpdate(state) {
  const song = state.currentSong;
  if (!song) return;

  updatePlayerUI(song, state.isPlaying);

  // Time & Progress Bar
  const curTimeEl = document.getElementById('time-current');
  const durTimeEl = document.getElementById('time-duration');
  const fillEl = document.getElementById('seek-bar-fill');

  const curMins = Math.floor(state.currentTime / 60);
  const curSecs = String(Math.floor(state.currentTime % 60)).padStart(2, '0');
  const durMins = Math.floor(state.duration / 60);
  const durSecs = String(Math.floor(state.duration % 60)).padStart(2, '0');

  if (curTimeEl) curTimeEl.textContent = `${curMins}:${curSecs}`;
  if (durTimeEl) durTimeEl.textContent = `${durMins}:${durSecs}`;
  if (fillEl) fillEl.style.width = `${state.progressPercent}%`;

  // Update Synced Lyrics active line
  updateSyncedLyrics(song, state.currentTime);
}

function updatePlayerUI(song, isPlaying) {
  const art = document.getElementById('player-art');
  const title = document.getElementById('player-title');
  const artist = document.getElementById('player-artist');
  const playIcon = document.getElementById('play-icon');
  const modalTrack = document.getElementById('modal-track-name');
  const heartBtn = document.getElementById('player-heart-btn');
  const modalHeartBtn = document.getElementById('modal-heart-btn');

  if (art) art.src = song.coverUrl;
  if (title) title.textContent = song.title;
  if (artist) artist.textContent = song.artist;
  if (modalTrack) modalTrack.textContent = `${song.title} - ${song.artist}`;

  const isFav = store.isFavorite(song);
  heartBtn?.classList.toggle('active', isFav);
  modalHeartBtn?.classList.toggle('active', isFav);

  if (playIcon) {
    playIcon.setAttribute('data-lucide', isPlaying ? 'pause' : 'play');
    if (window.lucide) window.lucide.createIcons();
  }

  // Update Trivia in Modal
  const triviaBox = document.getElementById('song-trivia-text');
  if (triviaBox) triviaBox.textContent = song.trivia || song.artistBio || 'Streamed in high quality via SongVerse.';

  const genreTag = document.getElementById('modal-genre-tag');
  if (genreTag) genreTag.textContent = song.genre || 'Global Music';

  // Re-render lyrics list if song changed
  if (lastRenderedLyricsSongId !== song.id) {
    renderLyricsList(song);
    lastRenderedLyricsSongId = song.id;
  }
}

function renderLyricsList(song) {
  const container = document.getElementById('lyrics-scroll-area');
  if (!container) return;
  const lyrics = song.syncedLyrics || [];

  container.innerHTML = lyrics.map((line, idx) => `
    <div class="lyric-line" data-line-time="${line.time}" data-line-index="${idx}">
      ${line.text}
    </div>
  `).join('');

  container.querySelectorAll('.lyric-line').forEach(lineEl => {
    lineEl.addEventListener('click', () => {
      const time = parseFloat(lineEl.dataset.lineTime);
      audioPlayer.seek(time);
    });
  });
}

function updateSyncedLyrics(song, currentTime) {
  const container = document.getElementById('lyrics-scroll-area');
  if (!container) return;
  const lines = song.syncedLyrics || [];
  if (lines.length === 0) return;

  let activeIdx = -1;
  for (let i = 0; i < lines.length; i++) {
    if (currentTime >= lines[i].time) {
      activeIdx = i;
    } else {
      break;
    }
  }

  const lineElements = container.querySelectorAll('.lyric-line');
  lineElements.forEach((el, i) => {
    const isAct = i === activeIdx;
    el.classList.toggle('active', isAct);
    if (isAct) {
      el.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
  });
}

function handleStoreUpdate(state) {
  applyTheme(state.theme);
}

// --- Modals Setup ---
function setupModals() {
  const fullPlayerModal = document.getElementById('full-player-modal');
  const openModalBtn = document.getElementById('open-player-modal-btn');
  const closeModalBtn = document.getElementById('close-player-modal-btn');
  const playerLeft = document.getElementById('player-left-toggle');
  const expandBtn = document.getElementById('expand-player-btn');

  const openFullPlayer = () => {
    fullPlayerModal?.classList.add('open');
    if (visualizer) visualizer.resize();
  };
  const closeFullPlayer = () => fullPlayerModal?.classList.remove('open');

  openModalBtn?.addEventListener('click', openFullPlayer);
  playerLeft?.addEventListener('click', openFullPlayer);
  expandBtn?.addEventListener('click', openFullPlayer);
  closeModalBtn?.addEventListener('click', closeFullPlayer);

  // Theme Modal
  const themeModal = document.getElementById('theme-modal');
  const themeBtn = document.getElementById('theme-btn');
  const closeThemeBtn = document.getElementById('close-theme-modal-btn');

  themeBtn?.addEventListener('click', () => themeModal?.classList.add('open'));
  closeThemeBtn?.addEventListener('click', () => themeModal?.classList.remove('open'));

  themeModal?.querySelectorAll('.theme-option-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      store.setTheme(btn.dataset.theme);
      themeModal?.classList.remove('open');
    });
  });

  // Playlist Modal
  const plModal = document.getElementById('playlist-modal');
  const newPlBtn = document.getElementById('new-playlist-btn');
  const cancelPlBtn = document.getElementById('cancel-playlist-btn');
  const savePlBtn = document.getElementById('save-playlist-btn');

  newPlBtn?.addEventListener('click', () => plModal?.classList.add('open'));
  cancelPlBtn?.addEventListener('click', () => plModal?.classList.remove('open'));
  savePlBtn?.addEventListener('click', () => {
    const nameInput = document.getElementById('playlist-name-input');
    const descInput = document.getElementById('playlist-desc-input');
    if (nameInput.value.trim()) {
      store.createPlaylist(nameInput.value.trim(), descInput.value.trim());
      nameInput.value = '';
      descInput.value = '';
      plModal?.classList.remove('open');
      renderLibrary();
    }
  });
}

// --- Visualizer Style Switcher Pills ---
function setupVisualizerPills() {
  document.querySelectorAll('.vis-pill').forEach(pill => {
    pill.addEventListener('click', () => {
      document.querySelectorAll('.vis-pill').forEach(p => p.classList.remove('active'));
      pill.classList.add('active');
      visualizer?.setStyle(pill.dataset.style);
    });
  });
}

// --- Global Keyboard Shortcuts ---
function setupKeyboardShortcuts() {
  window.addEventListener('keydown', (e) => {
    if (['INPUT', 'TEXTAREA'].includes(e.target.tagName)) return;

    if (e.code === 'Space') {
      e.preventDefault();
      audioPlayer.togglePlayPause();
    } else if (e.code === 'ArrowRight') {
      e.preventDefault();
      audioPlayer.seek(audioPlayer.currentTime + 5);
    } else if (e.code === 'ArrowLeft') {
      e.preventDefault();
      audioPlayer.seek(audioPlayer.currentTime - 5);
    } else if (e.code === 'KeyN') {
      audioPlayer.next();
    } else if (e.code === 'KeyP') {
      audioPlayer.previous();
    }
  });
}

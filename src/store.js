// src/store.js - Reactive LocalStorage Store for SongVerse

const STORAGE_KEYS = {
  FAVORITES: 'songverse_favorites',
  PLAYLISTS: 'songverse_playlists',
  HISTORY: 'songverse_history',
  RECENT_SEARCHES: 'songverse_recent_searches',
  THEME: 'songverse_theme',
  VISUALIZER_STYLE: 'songverse_vis_style',
  VOLUME: 'songverse_volume'
};

export const THEMES = {
  CYBERPUNK: {
    id: 'CYBERPUNK',
    name: 'Cyberpunk Neon',
    accent: '#00F0FF',
    accentSecondary: '#FF007F',
    bg: '#0a0a12',
    cardBg: 'rgba(20, 20, 35, 0.7)',
    textPrimary: '#FFFFFF',
    textSecondary: '#A0A0C0'
  },
  OCEAN: {
    id: 'OCEAN',
    name: 'Deep Ocean',
    accent: '#00E1D9',
    accentSecondary: '#3B82F6',
    bg: '#06131f',
    cardBg: 'rgba(12, 30, 48, 0.7)',
    textPrimary: '#FFFFFF',
    textSecondary: '#8CAEC4'
  },
  SUNSET: {
    id: 'SUNSET',
    name: 'Sunset Gold',
    accent: '#FFAA00',
    accentSecondary: '#FF3366',
    bg: '#180e0c',
    cardBg: 'rgba(38, 22, 20, 0.7)',
    textPrimary: '#FFFFFF',
    textSecondary: '#D0A398'
  },
  FOREST: {
    id: 'FOREST',
    name: 'Forest Emerald',
    accent: '#00FF87',
    accentSecondary: '#60EFFF',
    bg: '#081711',
    cardBg: 'rgba(14, 38, 28, 0.7)',
    textPrimary: '#FFFFFF',
    textSecondary: '#94CBB5'
  },
  MINIMAL_DARK: {
    id: 'MINIMAL_DARK',
    name: 'Midnight Studio',
    accent: '#E2E8F0',
    accentSecondary: '#94A3B8',
    bg: '#09090b',
    cardBg: 'rgba(24, 24, 27, 0.8)',
    textPrimary: '#FAFAFA',
    textSecondary: '#A1A1AA'
  }
};

class Store {
  constructor() {
    this.listeners = new Set();
    this.state = {
      favorites: this._loadJson(STORAGE_KEYS.FAVORITES, []),
      playlists: this._loadJson(STORAGE_KEYS.PLAYLISTS, this._getDefaultPlaylists()),
      history: this._loadJson(STORAGE_KEYS.HISTORY, []),
      recentSearches: this._loadJson(STORAGE_KEYS.RECENT_SEARCHES, ['Taylor Swift', 'Arijit Singh', 'The Weeknd', 'Synthwave']),
      theme: localStorage.getItem(STORAGE_KEYS.THEME) || 'CYBERPUNK',
      visualizerStyle: localStorage.getItem(STORAGE_KEYS.VISUALIZER_STYLE) || 'AURA',
      volume: parseFloat(localStorage.getItem(STORAGE_KEYS.VOLUME) || '0.8')
    };
  }

  _loadJson(key, defaultVal) {
    try {
      const data = localStorage.getItem(key);
      return data ? JSON.parse(data) : defaultVal;
    } catch {
      return defaultVal;
    }
  }

  _save(key, val) {
    try {
      localStorage.setItem(key, typeof val === 'string' ? val : JSON.stringify(val));
    } catch (e) {
      console.warn('Storage save failed:', e);
    }
    this._notify();
  }

  _notify() {
    this.listeners.forEach(cb => cb(this.state));
  }

  subscribe(cb) {
    this.listeners.add(cb);
    cb(this.state);
    return () => this.listeners.delete(cb);
  }

  _getDefaultPlaylists() {
    return [
      {
        id: 'pl-favorites',
        name: 'Liked Songs',
        description: 'Your favorite curated tracks',
        isDefault: true,
        songs: []
      },
      {
        id: 'pl-chill',
        name: 'Late Night Synth & Chill',
        description: 'Atmospheric vibes for midnight listening',
        isDefault: false,
        songs: []
      }
    ];
  }

  // --- Favorites ---
  isFavorite(song) {
    const id = `${song.title}-${song.artist}`.toLowerCase();
    return this.state.favorites.some(s => `${s.title}-${s.artist}`.toLowerCase() === id);
  }

  toggleFavorite(song) {
    const id = `${song.title}-${song.artist}`.toLowerCase();
    let updated;
    if (this.isFavorite(song)) {
      updated = this.state.favorites.filter(s => `${s.title}-${s.artist}`.toLowerCase() !== id);
    } else {
      updated = [{ ...song, isFavorite: true }, ...this.state.favorites];
    }
    this.state.favorites = updated;
    this._save(STORAGE_KEYS.FAVORITES, updated);
  }

  // --- History ---
  addToHistory(song) {
    const id = `${song.title}-${song.artist}`.toLowerCase();
    const filtered = this.state.history.filter(s => `${s.title}-${s.artist}`.toLowerCase() !== id);
    const updated = [song, ...filtered].slice(0, 50);
    this.state.history = updated;
    this._save(STORAGE_KEYS.HISTORY, updated);
  }

  clearHistory() {
    this.state.history = [];
    this._save(STORAGE_KEYS.HISTORY, []);
  }

  // --- Recent Searches ---
  addRecentSearch(query) {
    const q = query.trim();
    if (!q) return;
    const filtered = this.state.recentSearches.filter(item => item.toLowerCase() !== q.toLowerCase());
    const updated = [q, ...filtered].slice(0, 10);
    this.state.recentSearches = updated;
    this._save(STORAGE_KEYS.RECENT_SEARCHES, updated);
  }

  removeRecentSearch(query) {
    const updated = this.state.recentSearches.filter(item => item !== query);
    this.state.recentSearches = updated;
    this._save(STORAGE_KEYS.RECENT_SEARCHES, updated);
  }

  clearRecentSearches() {
    this.state.recentSearches = [];
    this._save(STORAGE_KEYS.RECENT_SEARCHES, []);
  }

  // --- Playlists ---
  createPlaylist(name, description = '') {
    const newPl = {
      id: 'pl-' + Date.now(),
      name: name.trim() || 'Untitled Playlist',
      description: description.trim(),
      isDefault: false,
      songs: []
    };
    const updated = [...this.state.playlists, newPl];
    this.state.playlists = updated;
    this._save(STORAGE_KEYS.PLAYLISTS, updated);
    return newPl;
  }

  deletePlaylist(playlistId) {
    const updated = this.state.playlists.filter(p => p.id !== playlistId || p.isDefault);
    this.state.playlists = updated;
    this._save(STORAGE_KEYS.PLAYLISTS, updated);
  }

  addSongToPlaylist(playlistId, song) {
    const updated = this.state.playlists.map(pl => {
      if (pl.id === playlistId) {
        const id = `${song.title}-${song.artist}`.toLowerCase();
        if (!pl.songs.some(s => `${s.title}-${s.artist}`.toLowerCase() === id)) {
          return { ...pl, songs: [...pl.songs, song] };
        }
      }
      return pl;
    });
    this.state.playlists = updated;
    this._save(STORAGE_KEYS.PLAYLISTS, updated);
  }

  removeSongFromPlaylist(playlistId, song) {
    const id = `${song.title}-${song.artist}`.toLowerCase();
    const updated = this.state.playlists.map(pl => {
      if (pl.id === playlistId) {
        return {
          ...pl,
          songs: pl.songs.filter(s => `${s.title}-${s.artist}`.toLowerCase() !== id)
        };
      }
      return pl;
    });
    this.state.playlists = updated;
    this._save(STORAGE_KEYS.PLAYLISTS, updated);
  }

  // --- Theme & Settings ---
  setTheme(themeId) {
    if (THEMES[themeId]) {
      this.state.theme = themeId;
      this._save(STORAGE_KEYS.THEME, themeId);
    }
  }

  setVisualizerStyle(style) {
    this.state.visualizerStyle = style;
    this._save(STORAGE_KEYS.VISUALIZER_STYLE, style);
  }

  setVolume(vol) {
    const clamped = Math.max(0, Math.min(1, vol));
    this.state.volume = clamped;
    this._save(STORAGE_KEYS.VOLUME, clamped.toString());
  }
}

export const store = new Store();

package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.data.Song
import com.example.data.api.*
import com.example.data.database.PlaylistEntity
import com.example.data.database.RecentSearchEntity
import com.example.data.repository.SongRepository
import com.example.ui.theme.AppThemePreset
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log

class SongViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SongRepository(application)

    // --- Theme State ---
    private val sharedPrefs = application.getSharedPreferences("songverse_prefs", android.content.Context.MODE_PRIVATE)
    private val _isDarkTheme = MutableStateFlow(sharedPrefs.getBoolean("is_dark_theme", true))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _selectedTheme = MutableStateFlow(
        AppThemePreset.valueOf(sharedPrefs.getString("selected_theme_preset", AppThemePreset.CYBERPUNK.name) ?: AppThemePreset.CYBERPUNK.name)
    )
    val selectedTheme: StateFlow<AppThemePreset> = _selectedTheme.asStateFlow()

    fun toggleTheme() {
        val newVal = !_isDarkTheme.value
        _isDarkTheme.value = newVal
        sharedPrefs.edit().putBoolean("is_dark_theme", newVal).apply()
    }

    fun setThemePreset(preset: AppThemePreset) {
        _selectedTheme.value = preset
        sharedPrefs.edit().putString("selected_theme_preset", preset.name).apply()
    }

    // --- State Observables ---

    val favorites: StateFlow<List<Song>> = repository.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<PlaylistEntity>> = repository.getPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentHistory: StateFlow<List<Song>> = repository.getRecentHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Active Selection States ---

    private val _selectedPlaylist = MutableStateFlow<PlaylistEntity?>(null)
    val selectedPlaylist: StateFlow<PlaylistEntity?> = _selectedPlaylist.asStateFlow()

    private val _selectedPlaylistSongs = MutableStateFlow<List<Song>>(emptyList())
    val selectedPlaylistSongs: StateFlow<List<Song>> = _selectedPlaylistSongs.asStateFlow()

    // --- Search States ---

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Song>>(emptyList())
    val searchResults: StateFlow<List<Song>> = _searchResults.asStateFlow()

    private val _discoverSongs = MutableStateFlow<List<Song>>(repository.preloadedSongs)
    val discoverSongs: StateFlow<List<Song>> = _discoverSongs.asStateFlow()

    private val _deviceSongs = MutableStateFlow<List<Song>>(emptyList())
    val deviceSongs: StateFlow<List<Song>> = _deviceSongs.asStateFlow()

    private val _isScanningDevice = MutableStateFlow(false)
    val isScanningDevice: StateFlow<Boolean> = _isScanningDevice.asStateFlow()

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()

    val recentSearches: StateFlow<List<RecentSearchEntity>> = repository.getRecentSearches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Audio Player Simulation States ---
    enum class AlbumArtStyle {
        AURA,      // Fluid mesh gradient
        VINYL,     // Rotating nostalgia vinyl record
        CYBERPUNK, // Neon perspective grid
        MINIMAL    // Sleek premium studio slate
    }

    private val _albumArtStyle = MutableStateFlow(AlbumArtStyle.AURA)
    val albumArtStyle: StateFlow<AlbumArtStyle> = _albumArtStyle.asStateFlow()

    fun setAlbumArtStyle(style: AlbumArtStyle) {
        _albumArtStyle.value = style
    }

    private val audioManager = application.getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager
    private val _volume = MutableStateFlow(0.7f) // Will be initialized correctly in init
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val volumeReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
            _volume.value = getCurrentVolumeFraction()
        }
    }

    private fun getCurrentVolumeFraction(): Float {
        val current = audioManager.getStreamVolume(android.media.AudioManager.STREAM_MUSIC)
        val max = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
        return if (max > 0) current.toFloat() / max else 0f
    }

    fun setVolume(fraction: Float) {
        val max = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
        val targetVolume = (fraction.coerceIn(0f, 1f) * max).toInt()
        audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, targetVolume, 0)
        _volume.value = fraction.coerceIn(0f, 1f)
    }

    private val _currentPlayingSong = MutableStateFlow<Song?>(null)
    val currentPlayingSong: StateFlow<Song?> = _currentPlayingSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0)
    val playbackProgress: StateFlow<Int> = _playbackProgress.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _isRepeatEnabled = MutableStateFlow(false)
    val isRepeatEnabled: StateFlow<Boolean> = _isRepeatEnabled.asStateFlow()

    // --- Dynamic Coil Dominant Color Extraction State ---
    private val defaultDominantColor = Color(0xFF1DB954) // Default Spotify Emerald Green
    private val _dominantColor = MutableStateFlow<Color>(defaultDominantColor)
    val dominantColor: StateFlow<Color> = _dominantColor.asStateFlow()

    private val _extractedSwatches = MutableStateFlow<List<Color>>(emptyList())
    val extractedSwatches: StateFlow<List<Color>> = _extractedSwatches.asStateFlow()

    fun extractDominantColorWithCoil(song: Song) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            var extractedColor: Color? = null
            val swatches = mutableListOf<Color>()

            if (!song.coverUrl.isNullOrBlank()) {
                try {
                    val request = ImageRequest.Builder(context)
                        .data(song.coverUrl)
                        .allowHardware(false) // Required for Palette software pixel access
                        .build()
                    val result = context.imageLoader.execute(request)
                    if (result is SuccessResult) {
                        val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
                        if (bitmap != null) {
                            val palette = Palette.from(bitmap).generate()

                            palette.dominantSwatch?.let { swatches.add(Color(it.rgb)) }
                            palette.vibrantSwatch?.let { swatches.add(Color(it.rgb)) }
                            palette.lightVibrantSwatch?.let { swatches.add(Color(it.rgb)) }
                            palette.darkVibrantSwatch?.let { swatches.add(Color(it.rgb)) }
                            palette.mutedSwatch?.let { swatches.add(Color(it.rgb)) }

                            val dominantSwatch = palette.dominantSwatch
                                ?: palette.vibrantSwatch
                                ?: palette.mutedSwatch
                                ?: palette.lightVibrantSwatch
                            if (dominantSwatch != null) {
                                extractedColor = Color(dominantSwatch.rgb)
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Fallback to signature color
                }
            }

            if (extractedColor == null) {
                val hash = kotlin.math.abs((song.title + song.artist + song.album).hashCode())
                val palettePresets = listOf(
                    listOf(Color(0xFF1DB954), Color(0xFF1ED760), Color(0xFF00E5FF)),
                    listOf(Color(0xFFFF3B30), Color(0xFFFF9500), Color(0xFFFF2A6D)),
                    listOf(Color(0xFF8E54E9), Color(0xFF4776E6), Color(0xFF00F5FF)),
                    listOf(Color(0xFF00E5FF), Color(0xFF3B82F6), Color(0xFF818CF8)),
                    listOf(Color(0xFF10B981), Color(0xFF34D399), Color(0xFF06B6D4)),
                    listOf(Color(0xFFFF5E3A), Color(0xFFFF2A6D), Color(0xFFFFB800))
                )
                val chosenGroup = palettePresets[hash % palettePresets.size]
                extractedColor = chosenGroup[0]
                swatches.addAll(chosenGroup)
            }

            withContext(Dispatchers.Main) {
                _dominantColor.value = extractedColor!!
                _extractedSwatches.value = swatches.distinct()
            }
        }
    }

    // --- Detailed Song Info & Sidebar States ---
    private val _detailedSongInfo = MutableStateFlow<GeminiDetailedSongInfo?>(null)
    val detailedSongInfo: StateFlow<GeminiDetailedSongInfo?> = _detailedSongInfo.asStateFlow()

    private val _isDetailedInfoLoading = MutableStateFlow(false)
    val isDetailedInfoLoading: StateFlow<Boolean> = _isDetailedInfoLoading.asStateFlow()

    fun loadDetailedSongInfo(song: Song) {
        // Set immediate detailed info for player screen
        _detailedSongInfo.value = getFallbackDetailedInfo(
            title = song.title,
            artist = song.artist,
            album = song.album,
            releaseYear = song.releaseYear,
            trivia = song.trivia
        )
        _isDetailedInfoLoading.value = false
    }

    // Playback queue and history tracking
    private val _playbackQueueState = MutableStateFlow<List<Song>>(emptyList())
    val playbackQueueState: StateFlow<List<Song>> = _playbackQueueState.asStateFlow()

    private val _currentQueueIndexState = MutableStateFlow(-1)
    val currentQueueIndexState: StateFlow<Int> = _currentQueueIndexState.asStateFlow()

    private var originalQueue: List<Song> = emptyList()
    private var playbackQueue: List<Song>
        get() = _playbackQueueState.value
        set(value) {
            _playbackQueueState.value = value
        }
    private var currentQueueIndex: Int
        get() = _currentQueueIndexState.value
        set(value) {
            _currentQueueIndexState.value = value
        }

    fun addSongToQueue(song: Song) {
        val currentList = playbackQueue.toMutableList()
        currentList.add(song)
        playbackQueue = currentList

        val currentOrig = originalQueue.toMutableList()
        currentOrig.add(song)
        originalQueue = currentOrig

        if (_currentPlayingSong.value == null || currentQueueIndex == -1) {
            currentQueueIndex = playbackQueue.size - 1
            playSongFromQueue(song)
        }
    }

    fun removeSongFromQueue(index: Int) {
        if (index < 0 || index >= playbackQueue.size) return

        val songToRemove = playbackQueue[index]
        val currentList = playbackQueue.toMutableList()
        currentList.removeAt(index)

        val currentOrig = originalQueue.toMutableList()
        val origIndex = currentOrig.indexOf(songToRemove)
        if (origIndex != -1) {
            currentOrig.removeAt(origIndex)
        }
        originalQueue = currentOrig

        if (currentList.isEmpty()) {
            playbackQueue = emptyList()
            currentQueueIndex = -1
            _currentPlayingSong.value = null
            stopAudio()
            _isPlaying.value = false
            _playbackProgress.value = 0
        } else {
            if (currentQueueIndex == index) {
                playbackQueue = currentList
                if (currentQueueIndex >= currentList.size) {
                    currentQueueIndex = 0
                }
                playSongFromQueue(currentList[currentQueueIndex])
            } else {
                val oldIndex = currentQueueIndex
                if (index < oldIndex) {
                    currentQueueIndex = oldIndex - 1
                }
                playbackQueue = currentList
            }
        }
    }

    fun clearQueue() {
        originalQueue = emptyList()
        playbackQueue = emptyList()
        currentQueueIndex = -1
        _currentPlayingSong.value = null
        stopAudio()
        _isPlaying.value = false
        _playbackProgress.value = 0
    }

    fun playQueueSong(index: Int) {
        if (index >= 0 && index < playbackQueue.size) {
            currentQueueIndex = index
            playSongFromQueue(playbackQueue[index])
        }
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        if (fromIndex < 0 || fromIndex >= playbackQueue.size) return
        if (toIndex < 0 || toIndex >= playbackQueue.size) return

        val currentList = playbackQueue.toMutableList()
        val song = currentList.removeAt(fromIndex)
        currentList.add(toIndex, song)

        val playingSong = _currentPlayingSong.value
        playbackQueue = currentList
        if (playingSong != null) {
            currentQueueIndex = currentList.indexOfFirst { it.title == playingSong.title && it.artist == playingSong.artist }
        }
    }

    private var playerTimerJob: Job? = null

    init {
        _volume.value = getCurrentVolumeFraction()
        val filter = android.content.IntentFilter("android.media.VOLUME_CHANGED_ACTION")
        try {
            application.registerReceiver(volumeReceiver, filter)
        } catch (e: Exception) {
            // ignore
        }

        // Initialize Discover and Search with rich preloaded library
        _discoverSongs.value = repository.preloadedSongs
        _searchResults.value = repository.preloadedSongs

        // Concurrently fetch trending world hits and merge into discover & search
        viewModelScope.launch {
            try {
                val trending = repository.fetchTrendingWorldSongs()
                if (trending.isNotEmpty()) {
                    val combined = (repository.preloadedSongs + trending).distinctBy {
                        "${it.title.lowercase().trim()}-${it.artist.lowercase().trim()}"
                    }
                    _discoverSongs.value = combined
                    _searchResults.value = combined
                }
            } catch (e: Exception) {
                // Keep preloaded library
            }
        }

        // Scan local device audio on startup
        scanDeviceMusic()

        // Automatically load detailed info and extract Coil dominant color when current song changes
        viewModelScope.launch {
            _currentPlayingSong.collect { song ->
                if (song != null) {
                    loadDetailedSongInfo(song)
                    extractDominantColorWithCoil(song)
                }
            }
        }

        // Listen for current song favorite updates from the repository
        viewModelScope.launch {
            _currentPlayingSong.collect { song ->
                if (song != null) {
                    repository.isFavoriteFlow(song.title, song.artist).collect { fav ->
                        if (_currentPlayingSong.value?.title == song.title) {
                            _currentPlayingSong.value = _currentPlayingSong.value?.copy(isFavorite = fav)
                        }
                    }
                }
            }
        }
    }

    fun scanDeviceMusic() {
        viewModelScope.launch(Dispatchers.IO) {
            _isScanningDevice.value = true
            try {
                val songs = repository.scanDeviceAudio()
                _deviceSongs.value = songs
            } catch (e: Exception) {
                Log.e("SongViewModel", "Failed to scan device audio: ${e.localizedMessage}")
            } finally {
                _isScanningDevice.value = false
            }
        }
    }

    // --- Search Actions ---
    private var searchJob: Job? = null

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _searchError.value = null
        searchJob?.cancel()

        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            _isSearching.value = false
            searchJob = viewModelScope.launch {
                val trending = repository.fetchTrendingWorldSongs()
                _searchResults.value = if (trending.isNotEmpty()) trending else repository.preloadedSongs
            }
            return
        }

        searchJob = viewModelScope.launch {
            _isSearching.value = true
            delay(150) // Ultra-fast debounce
            try {
                val results = repository.searchAllSongs(trimmed)
                if (results.isEmpty()) {
                    _searchError.value = "No songs found matching '$trimmed'."
                }
                _searchResults.value = results
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    _searchError.value = "Search failed: ${e.localizedMessage}"
                    _searchResults.value = emptyList()
                }
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun addRecentSearch(query: String) {
        viewModelScope.launch {
            repository.addRecentSearch(query)
        }
    }

    fun removeRecentSearch(query: String) {
        viewModelScope.launch {
            repository.removeRecentSearch(query)
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch {
            repository.clearRecentSearches()
        }
    }

    // --- Playlist Actions ---

    fun createPlaylist(name: String, description: String) {
        viewModelScope.launch {
            repository.createPlaylist(name, description)
        }
    }

    fun deletePlaylist(playlistId: Int) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
            if (_selectedPlaylist.value?.id == playlistId) {
                _selectedPlaylist.value = null
                _selectedPlaylistSongs.value = emptyList()
            }
        }
    }

    fun selectPlaylist(playlist: PlaylistEntity?) {
        _selectedPlaylist.value = playlist
        if (playlist == null) {
            _selectedPlaylistSongs.value = emptyList()
            return
        }
        viewModelScope.launch {
            repository.getPlaylistSongs(playlist.id).collect { songs ->
                _selectedPlaylistSongs.value = songs
            }
        }
    }

    fun addSongToPlaylist(playlistId: Int, song: Song) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, song)
        }
    }

    fun removeSongFromPlaylist(playlistSongId: Int) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistSongId)
        }
    }

    fun removeSongFromPlaylist(playlistId: Int, song: Song) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, song.title, song.artist)
        }
    }

    // --- Player Control Actions ---

    // --- Real Audio Player Integration ---
    private var mediaPlayer: android.media.MediaPlayer? = null

    fun playSong(song: Song, contextQueue: List<Song> = emptyList()) {
        viewModelScope.launch {
            repository.addRecentSong(song)
        }

        _currentPlayingSong.value = song
        _playbackProgress.value = 0

        // Set up playback queue
        if (contextQueue.isNotEmpty() && contextQueue.contains(song)) {
            originalQueue = contextQueue
            rebuildQueue()
            currentQueueIndex = playbackQueue.indexOfFirst { it.title == song.title && it.artist == song.artist }
        } else {
            // Standalone play
            originalQueue = listOf(song)
            playbackQueue = listOf(song)
            currentQueueIndex = 0
        }

        startRealAudioPlayback(song)
    }

    fun togglePlayPause() {
        if (_currentPlayingSong.value == null) {
            // Play first preloaded or search result if none is playing
            val defaultSong = _searchResults.value.firstOrNull() ?: repository.preloadedSongs.firstOrNull()
            if (defaultSong != null) {
                playSong(defaultSong, _searchResults.value)
            }
            return
        }

        val mp = mediaPlayer
        if (mp != null) {
            if (mp.isPlaying) {
                mp.pause()
                _isPlaying.value = false
                playerTimerJob?.cancel()
            } else {
                mp.start()
                _isPlaying.value = true
                startProgressTimer()
            }
        } else {
            val nextState = !_isPlaying.value
            _isPlaying.value = nextState
            if (nextState) startProgressTimer() else playerTimerJob?.cancel()
        }
    }

    fun skipNext() {
        if (playbackQueue.isEmpty() || currentQueueIndex == -1) return

        var nextIndex = currentQueueIndex + 1
        if (nextIndex >= playbackQueue.size) {
            nextIndex = if (_isRepeatEnabled.value) 0 else -1
        }

        if (nextIndex != -1) {
            currentQueueIndex = nextIndex
            playSongFromQueue(playbackQueue[currentQueueIndex])
        } else {
            stopAudio()
            _isPlaying.value = false
            _playbackProgress.value = 0
        }
    }

    fun skipPrevious() {
        if (playbackQueue.isEmpty() || currentQueueIndex == -1) return

        if (_playbackProgress.value > 3) {
            seekTo(0)
            return
        }

        var prevIndex = currentQueueIndex - 1
        if (prevIndex < 0) {
            prevIndex = if (_isRepeatEnabled.value) playbackQueue.size - 1 else 0
        }

        currentQueueIndex = prevIndex
        playSongFromQueue(playbackQueue[currentQueueIndex])
    }

    fun toggleShuffle() {
        _isShuffleEnabled.value = !_isShuffleEnabled.value
        val currentSong = _currentPlayingSong.value
        rebuildQueue()
        if (currentSong != null) {
            currentQueueIndex = playbackQueue.indexOfFirst { it.title == currentSong.title && it.artist == currentSong.artist }
        }
    }

    fun toggleRepeat() {
        _isRepeatEnabled.value = !_isRepeatEnabled.value
    }

    fun seekTo(seconds: Int) {
        val maxDuration = _currentPlayingSong.value?.durationSeconds ?: 180
        val clampedSec = seconds.coerceIn(0, maxDuration)
        _playbackProgress.value = clampedSec

        mediaPlayer?.let { mp ->
            try {
                mp.seekTo(clampedSec * 1000)
            } catch (e: Exception) {
                Log.e("SongViewModel", "Error seeking media player", e)
            }
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            repository.toggleFavorite(song)
            if (_currentPlayingSong.value?.title == song.title && _currentPlayingSong.value?.artist == song.artist) {
                val isFav = repository.isFavorite(song.title, song.artist)
                _currentPlayingSong.value = _currentPlayingSong.value?.copy(isFavorite = isFav)
            }
        }
    }

    fun clearPlaybackHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // --- Audio Player Helpers ---

    private fun startRealAudioPlayback(song: Song) {
        playerTimerJob?.cancel()
        stopAudio()

        if (song.audioUrl.isNotBlank()) {
            try {
                mediaPlayer = android.media.MediaPlayer().apply {
                    setAudioAttributes(
                        android.media.AudioAttributes.Builder()
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(song.audioUrl)
                    prepareAsync()
                    setOnPreparedListener { mp ->
                        mp.start()
                        _isPlaying.value = true
                        startProgressTimer()
                    }
                    setOnCompletionListener {
                        skipNext()
                    }
                    setOnErrorListener { _, _, _ ->
                        startProgressTimer()
                        true
                    }
                }
            } catch (e: Exception) {
                Log.e("SongViewModel", "Failed to start real audio playback", e)
                startProgressTimer()
            }
        } else {
            startProgressTimer()
        }
    }

    private fun startProgressTimer() {
        _isPlaying.value = true
        playerTimerJob?.cancel()
        playerTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val mp = mediaPlayer
                if (mp != null) {
                    try {
                        if (mp.isPlaying) {
                            _playbackProgress.value = mp.currentPosition / 1000
                        }
                    } catch (e: Exception) {
                        Log.e("SongViewModel", "Error updating playback progress", e)
                    }
                } else if (_isPlaying.value) {
                    val currentProgress = _playbackProgress.value
                    val maxDuration = _currentPlayingSong.value?.durationSeconds ?: 180

                    if (currentProgress < maxDuration) {
                        _playbackProgress.value = currentProgress + 1
                    } else {
                        if (_isRepeatEnabled.value && playbackQueue.size == 1) {
                            _playbackProgress.value = 0
                        } else {
                            skipNext()
                        }
                        break
                    }
                }
            }
        }
    }

    private fun stopAudio() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e("SongViewModel", "Error stopping audio", e)
        }
    }

    private fun playSongFromQueue(song: Song) {
        _currentPlayingSong.value = song
        _playbackProgress.value = 0
        viewModelScope.launch {
            repository.addRecentSong(song)
        }
        startRealAudioPlayback(song)
    }

    private fun rebuildQueue() {
        playbackQueue = if (_isShuffleEnabled.value) {
            originalQueue.shuffled()
        } else {
            originalQueue
        }
    }

    // --- Playlist Import Logic ---

    private val _importState = MutableStateFlow<ImportPlaylistState>(ImportPlaylistState.Idle)
    val importState: StateFlow<ImportPlaylistState> = _importState.asStateFlow()

    fun resetImportState() {
        _importState.value = ImportPlaylistState.Idle
    }

    fun parsePlaylist(source: String, input: String) {
        if (input.trim().isEmpty()) {
            _importState.value = ImportPlaylistState.Error("Please enter a playlist URL or a list of songs.")
            return
        }

        _importState.value = ImportPlaylistState.Loading

        viewModelScope.launch {
            try {
                val (name, songs) = parsePlaylistOffline(source, input)
                _importState.value = ImportPlaylistState.Success(
                    name = name,
                    description = "Imported from $source",
                    songs = songs,
                    isFromAi = false
                )
            } catch (e: Exception) {
                _importState.value = ImportPlaylistState.Error("Error: ${e.localizedMessage ?: "Failed to import"}")
            }
        }
    }

    private fun parsePlaylistOffline(source: String, input: String): Pair<String, List<Song>> {
        val name = when {
            input.contains("http") -> "Imported $source Playlist"
            else -> "My Imported Playlist"
        }
        val description = "Imported offline from $source on ${java.text.DateFormat.getDateInstance().format(java.util.Date())}"
        
        val songsList = mutableListOf<Song>()
        val lines = input.lines()
        for (line in lines) {
            val cleanLine = line.trim()
            if (cleanLine.isEmpty() || cleanLine.startsWith("http")) continue
            
            var parsedTitle = cleanLine
            var parsedArtist = "Unknown Artist"
            
            val splitByHyphen = cleanLine.split(" - ", limit = 2)
            val splitByBy = cleanLine.split(" by ", limit = 2)
            
            if (splitByHyphen.size == 2) {
                parsedTitle = splitByHyphen[0].trim()
                parsedArtist = splitByHyphen[1].trim()
            } else if (splitByBy.size == 2) {
                parsedTitle = splitByBy[0].trim()
                parsedArtist = splitByBy[1].trim()
            }
            
            // Try to match with preloaded songs
            val matchedSong = repository.preloadedSongs.firstOrNull { 
                it.title.equals(parsedTitle, ignoreCase = true) || 
                it.title.contains(parsedTitle, ignoreCase = true) ||
                parsedTitle.contains(it.title, ignoreCase = true)
            }
            
            if (matchedSong != null) {
                songsList.add(matchedSong)
            } else {
                songsList.add(
                    Song(
                        title = parsedTitle,
                        artist = parsedArtist,
                        album = "Single",
                        durationSeconds = 180,
                        genre = "Imported",
                        releaseYear = "2026",
                        lyrics = "[Offline Import] Enjoy listening to $parsedTitle!",
                        trivia = "Imported track from your $source playlist.",
                        tempoBpm = 110,
                        mood = "Uplifting",
                        energy = 0.6f
                    )
                )
            }
        }
        
        if (songsList.isEmpty()) {
            songsList.addAll(repository.preloadedSongs.take(4))
        }
        
        return Pair(name, songsList)
    }

    fun confirmImportPlaylist(name: String, description: String, songs: List<Song>) {
        viewModelScope.launch {
            repository.importPlaylist(name, description, songs)
            _importState.value = ImportPlaylistState.Idle
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAudio()
        try {
            getApplication<android.app.Application>().unregisterReceiver(volumeReceiver)
        } catch (e: Exception) {
            // ignore
        }
    }
}

sealed class ImportPlaylistState {
    object Idle : ImportPlaylistState()
    object Loading : ImportPlaylistState()
    data class Success(
        val name: String,
        val description: String,
        val songs: List<Song>,
        val isFromAi: Boolean
    ) : ImportPlaylistState()
    data class Error(val message: String) : ImportPlaylistState()
}

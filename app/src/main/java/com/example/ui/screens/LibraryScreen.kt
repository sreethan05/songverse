package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.example.ui.viewmodel.ImportPlaylistState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Song
import com.example.data.database.PlaylistEntity
import com.example.ui.components.PremiumAlbumArt
import com.example.ui.theme.*
import com.example.ui.viewmodel.SongViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: SongViewModel,
    modifier: Modifier = Modifier
) {
    val favorites by viewModel.favorites.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val selectedPlaylist by viewModel.selectedPlaylist.collectAsState()
    val selectedPlaylistSongs by viewModel.selectedPlaylistSongs.collectAsState()
    val albumArtStyle by viewModel.albumArtStyle.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0 = Favorites, 1 = Playlists
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showImportPlaylistDialog by remember { mutableStateOf(false) }
    val importState by viewModel.importState.collectAsState()

    // Manage back button navigation inside Library Screen when a playlist is open
    if (selectedPlaylist != null) {
        BackHandler {
            viewModel.selectPlaylist(null)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (selectedPlaylist == null) {
            // --- MAIN LIBRARY VIEW ---
            Column(modifier = Modifier.fillMaxSize()) {
                // Tab Selection
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TabHeaderButton(
                        text = "Favorites",
                        isActive = activeTab == 0,
                        onClick = { activeTab = 0 },
                        badgeCount = favorites.size
                    )

                    TabHeaderButton(
                        text = "Playlists",
                        isActive = activeTab == 1,
                        onClick = { activeTab = 1 },
                        badgeCount = playlists.size
                    )
                }

                if (activeTab == 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { showCreatePlaylistDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonPurple),
                            border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.3f)),
                            modifier = Modifier.weight(1f).height(40.dp).testTag("create_playlist_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("New Playlist", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showImportPlaylistDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                            modifier = Modifier.weight(1f).height(40.dp).testTag("import_playlist_btn")
                        ) {
                            Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import Playlist", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.weight(1f)) {
                    if (activeTab == 0) {
                        // --- FAVORITES LIST ---
                        if (favorites.isEmpty()) {
                            EmptyLibraryState(
                                icon = Icons.Default.FavoriteBorder,
                                title = "Your Favorites is Empty",
                                description = "Tap the heart icon in the player or search to save songs here."
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 120.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(favorites) { song ->
                                    LibrarySongRow(
                                        song = song,
                                        style = albumArtStyle,
                                        isPlaying = isPlaying,
                                        isPlayingNow = viewModel.currentPlayingSong.value?.title == song.title,
                                        actionIcon = Icons.Default.Favorite,
                                        actionIconTint = NeonMagenta,
                                        onActionClick = { viewModel.toggleFavorite(song) },
                                        onPlay = { viewModel.playSong(song, favorites) }
                                    )
                                }
                            }
                        }
                    } else {
                        // --- PLAYLISTS GRID/LIST ---
                        if (playlists.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                EmptyLibraryState(
                                    icon = Icons.Default.QueueMusic,
                                    title = "No Playlists Yet",
                                    description = "Create your custom soundtracks. Click the floating button below to begin!"
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 120.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(playlists) { playlist ->
                                    PlaylistRow(
                                        playlist = playlist,
                                        onClick = { viewModel.selectPlaylist(playlist) },
                                        onDelete = { viewModel.deletePlaylist(playlist.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Create Playlist FAB
            AnimatedVisibility(
                visible = activeTab == 1,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 96.dp, end = 16.dp)
            ) {
                FloatingActionButton(
                    onClick = { showCreatePlaylistDialog = true },
                    containerColor = NeonPurple,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("create_playlist_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "New Playlist")
                }
            }

        } else {
            // --- PLAYLIST DETAILS SCREEN ---
            PlaylistDetailsView(
                playlist = selectedPlaylist!!,
                songs = selectedPlaylistSongs,
                onBack = {
                    viewModel.selectPlaylist(null)
                },
                viewModel = viewModel
            )
        }
    }

    // --- Create Playlist Dialog ---
    if (showCreatePlaylistDialog) {
        var playlistName by remember { mutableStateOf("") }
        var playlistDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            title = { Text("New Playlist", fontWeight = FontWeight.Bold, color = TextPrimary) },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = playlistName,
                        onValueChange = { playlistName = it },
                        label = { Text("Playlist Name", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = NeonCyan
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("playlist_name_input")
                    )

                    OutlinedTextField(
                        value = playlistDesc,
                        onValueChange = { playlistDesc = it },
                        label = { Text("Description (Optional)", color = TextSecondary) },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = NeonCyan
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("playlist_desc_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (playlistName.trim().isNotEmpty()) {
                            viewModel.createPlaylist(playlistName, playlistDesc)
                            showCreatePlaylistDialog = false
                            playlistName = ""
                            playlistDesc = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                ) {
                    Text("Create", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylistDialog = false }) {
                    Text("Cancel", color = NeonCyan)
                }
            }
        )
    }

    // --- Import Playlist Dialog ---
    if (showImportPlaylistDialog) {
        var selectedSource by remember { mutableStateOf("Spotify") }
        var playlistInput by remember { mutableStateOf("") }

        // Sources list: Name, Color, Icon
        val sources = listOf(
            Triple("Spotify", Color(0xFF1DB954), Icons.Default.MusicNote),
            Triple("YouTube Music", Color(0xFFFF0000), Icons.Default.PlayArrow),
            Triple("Apple Music", Color(0xFFFC3C44), Icons.Default.Favorite),
            Triple("Text List", NeonCyan, Icons.Default.List)
        )

        AlertDialog(
            onDismissRequest = { 
                showImportPlaylistDialog = false 
                viewModel.resetImportState()
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        tint = NeonPurple,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Import Playlist", fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            text = {
                Box(modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp)) {
                    when (val state = importState) {
                        is ImportPlaylistState.Idle -> {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Select source and paste your playlist URL, share link, or raw text list of songs:",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )

                                // Horizontal selector for apps
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    sources.forEach { (sourceName, sourceColor, sourceIcon) ->
                                        val isSel = selectedSource == sourceName
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (isSel) sourceColor.copy(alpha = 0.2f)
                                                    else MaterialTheme.colorScheme.surface
                                                )
                                                .clickable { selectedSource = sourceName }
                                                .padding(vertical = 8.dp)
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(
                                                    imageVector = sourceIcon,
                                                    contentDescription = sourceName,
                                                    tint = if (isSel) sourceColor else TextSecondary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = sourceName,
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSel) TextPrimary else TextSecondary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = playlistInput,
                                    onValueChange = { playlistInput = it },
                                    label = {
                                        Text(
                                            if (selectedSource == "Text List") "Paste song list (one per line)" 
                                            else "Paste $selectedSource playlist link or songs", 
                                            color = TextSecondary
                                        )
                                    },
                                    placeholder = {
                                        Text(
                                            if (selectedSource == "Text List") "e.g.\nBlinding Lights - The Weeknd\nStay - Justin Bieber"
                                            else "e.g. https://open.spotify.com/playlist/37i9dQZF1DX10zKxhJg7as",
                                            color = TextSecondary.copy(alpha = 0.5f)
                                        )
                                    },
                                    minLines = 4,
                                    maxLines = 6,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NeonPurple,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                        focusedLabelColor = NeonPurple
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("playlist_import_input")
                                )
                            }
                        }

                        is ImportPlaylistState.Loading -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                            ) {
                                CircularProgressIndicator(color = NeonPurple, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = "AI is parsing your playlist...",
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Analyzing links, matching tracks, generating lyrics, and collecting trivia. This takes a moment...",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }

                        is ImportPlaylistState.Success -> {
                            var importedName by remember { mutableStateOf(state.name) }
                            var importedDesc by remember { mutableStateOf(state.description) }
                            val selectedSongs = remember { mutableStateMapOf<Song, Boolean>().apply {
                                state.songs.forEach { this[it] = true }
                            }}

                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Import Preview",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = TextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (state.isFromAi) {
                                        Surface(
                                            color = NeonCyan.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.padding(start = 8.dp)
                                        ) {
                                            Text(
                                                text = "GEMINI AI POWERED",
                                                color = NeonCyan,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    } else {
                                        Surface(
                                            color = Color.Gray.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.padding(start = 8.dp)
                                        ) {
                                            Text(
                                                text = "OFFLINE PARSER",
                                                color = TextSecondary,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = importedName,
                                    onValueChange = { importedName = it },
                                    label = { Text("Playlist Name", color = TextSecondary) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NeonPurple,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                        focusedLabelColor = NeonPurple
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("imported_playlist_name")
                                )

                                OutlinedTextField(
                                    value = importedDesc,
                                    onValueChange = { importedDesc = it },
                                    label = { Text("Description", color = TextSecondary) },
                                    maxLines = 2,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NeonPurple,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                        focusedLabelColor = NeonPurple
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Text(
                                    text = "Select songs to import (${selectedSongs.values.count { it }} selected):",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
                                        .padding(8.dp)
                                ) {
                                    LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(state.songs) { song ->
                                            val isChecked = selectedSongs[song] ?: false
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { selectedSongs[song] = !isChecked }
                                                    .padding(vertical = 4.dp)
                                            ) {
                                                Checkbox(
                                                    checked = isChecked,
                                                    onCheckedChange = { selectedSongs[song] = it },
                                                    colors = CheckboxDefaults.colors(checkedColor = NeonPurple)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = song.title,
                                                        fontSize = 13.sp,
                                                        color = TextPrimary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = song.artist,
                                                        fontSize = 11.sp,
                                                        color = TextSecondary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = song.genre,
                                                    fontSize = 11.sp,
                                                    color = NeonCyan,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TextButton(
                                        onClick = { viewModel.resetImportState() },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Back", color = NeonCyan)
                                    }
                                    Button(
                                        onClick = {
                                            val finalSongs = state.songs.filter { selectedSongs[it] == true }
                                            if (finalSongs.isNotEmpty() && importedName.trim().isNotEmpty()) {
                                                viewModel.confirmImportPlaylist(importedName, importedDesc, finalSongs)
                                                showImportPlaylistDialog = false
                                                playlistInput = ""
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                                        modifier = Modifier.weight(1.5f).testTag("confirm_import_button")
                                    ) {
                                        Text("Save Playlist", color = Color.White)
                                    }
                                }
                            }
                        }

                        is ImportPlaylistState.Error -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = NeonMagenta,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Import Failed",
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.message,
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    TextButton(onClick = { viewModel.resetImportState() }) {
                                        Text("Try Offline Mode", color = NeonCyan)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (importState is ImportPlaylistState.Idle) {
                    Button(
                        onClick = {
                            if (playlistInput.trim().isNotEmpty()) {
                                viewModel.parsePlaylist(selectedSource, playlistInput)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                        modifier = Modifier.testTag("import_parse_button")
                    ) {
                        Text("Import", color = Color.White)
                    }
                }
            },
            dismissButton = {
                if (importState is ImportPlaylistState.Idle) {
                    TextButton(onClick = { 
                        showImportPlaylistDialog = false 
                        viewModel.resetImportState()
                    }) {
                        Text("Cancel", color = NeonCyan)
                    }
                }
            }
        )
    }
}

@Composable
fun TabHeaderButton(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit,
    badgeCount: Int
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isActive) NeonPurple.copy(alpha = 0.25f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                color = if (isActive) NeonCyan else TextSecondary
            )

            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (isActive) NeonMagenta else Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeCount.toString(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun PlaylistRow(
    playlist: PlaylistEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(NeonPurple, NeonMagenta)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.QueueMusic,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = if (playlist.description.isEmpty()) "Custom Playlist" else playlist.description,
                fontSize = 12.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Playlist",
                tint = NeonMagenta.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun LibrarySongRow(
    song: Song,
    isPlayingNow: Boolean,
    actionIcon: androidx.compose.ui.graphics.vector.ImageVector,
    actionIconTint: Color,
    onActionClick: () -> Unit,
    onPlay: () -> Unit,
    style: SongViewModel.AlbumArtStyle = SongViewModel.AlbumArtStyle.AURA,
    isPlaying: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onPlay() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PremiumAlbumArt(
            title = song.title,
            artist = song.artist,
            modifier = Modifier.size(48.dp),
            cornerRadius = 8,
            style = style,
            isPlaying = isPlaying && isPlayingNow
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPlayingNow) NeonCyan else TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${song.artist} • ${song.album}",
                fontSize = 11.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(onClick = onActionClick) {
            Icon(
                imageVector = actionIcon,
                contentDescription = "Song Row Action",
                tint = actionIconTint
            )
        }
    }
}

@Composable
fun EmptyLibraryState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = NeonPurple.copy(alpha = 0.3f),
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            fontSize = 12.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PlaylistDetailsView(
    playlist: PlaylistEntity,
    songs: List<Song>,
    onBack: () -> Unit,
    viewModel: SongViewModel
) {
    val albumArtStyle by viewModel.albumArtStyle.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Back Navigation Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = NeonCyan
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = playlist.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Playlist Metadata Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(listOf(NeonPurple, NeonCyan))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "PLAYLIST",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = playlist.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    if (playlist.description.isNotEmpty()) {
                        Text(
                            text = playlist.description,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Songs list
        if (songs.isEmpty()) {
            EmptyLibraryState(
                icon = Icons.Default.MusicNote,
                title = "No Tracks in Playlist",
                description = "Search for tracks in the Search tab and select 'Add to Playlist' to fill this soundtrack!"
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${songs.size} Tracks",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )

                Button(
                    onClick = { viewModel.playSong(songs.first(), songs) },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Text("PLAY ALL", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

             LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 120.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(songs) { song ->
                    LibrarySongRow(
                        song = song,
                        style = albumArtStyle,
                        isPlaying = isPlaying,
                        isPlayingNow = viewModel.currentPlayingSong.value?.title == song.title,
                        actionIcon = Icons.Default.Delete,
                        actionIconTint = TextSecondary,
                        onActionClick = {
                            viewModel.removeSongFromPlaylist(playlist.id, song)
                        },
                        onPlay = { viewModel.playSong(song, songs) }
                    )
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Song
import com.example.ui.components.PremiumAlbumArt
import com.example.ui.theme.*
import com.example.ui.viewmodel.SongViewModel

@Composable
fun DiscoverScreen(
    viewModel: SongViewModel,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recentHistory by viewModel.recentHistory.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val albumArtStyle by viewModel.albumArtStyle.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    // Observe rich discover songs catalog
    val discoverSongs by viewModel.discoverSongs.collectAsState()
    var selectedGenre by remember { mutableStateOf("All") }
    val genres = listOf("All", "Bollywood", "Punjabi", "Pop", "Rock", "Synthwave", "Jazz", "Classical")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // --- 1. AI Hero Banner ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(NeonPurple, NeonMagenta, NeonCyan),
                            start = Offset(0f, 0f),
                            end = Offset(1000f, 500f)
                        )
                    )
                    .clickable { onNavigateToSearch() }
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "GLOBAL AI MUSIC SEARCH",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 1.5.sp
                        )
                    }

                    Column {
                        Text(
                            text = "Find Any Song in the World",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Search any song title, artist, or movie album to stream live for free with JioSaavn.",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // --- 2. Featured Track (Glowing Spotlight) ---
        item {
            Text(
                text = "Spotlight Release",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            val spotlightSong = discoverSongs.firstOrNull { it.title.contains("Kesariya", ignoreCase = true) } ?: discoverSongs.firstOrNull()
            if (spotlightSong != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .drawBehind {
                            drawRect(
                                Brush.linearGradient(
                                    colors = listOf(NeonCyan.copy(alpha = 0.15f), Color.Transparent),
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, size.height)
                                )
                            )
                        }
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { viewModel.playSong(spotlightSong, discoverSongs) }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Album Art Box
                        PremiumAlbumArt(
                            title = spotlightSong.title,
                            artist = spotlightSong.artist,
                            modifier = Modifier.size(72.dp),
                            cornerRadius = 12,
                            style = albumArtStyle,
                            isPlaying = isPlaying && (viewModel.currentPlayingSong.value?.title == spotlightSong.title)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        // Song Details
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = spotlightSong.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = spotlightSong.artist,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(onClick = { viewModel.toggleFavorite(spotlightSong) }) {
                            val isFav = favorites.any { it.title == spotlightSong.title && it.artist == spotlightSong.artist }
                            Icon(
                                imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFav) NeonMagenta else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // --- 3. Genre Quick Shelves ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Explore Genres & Moods",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(end = 16.dp)
                ) {
                    items(genres) { genre ->
                        val isSelected = selectedGenre.equals(genre, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.clickable {
                                selectedGenre = genre
                            }
                        ) {
                            Text(
                                text = genre,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- 4. Recently Played Shelf (Room Persistence) ---
        if (recentHistory.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Recently Played",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    TextButton(onClick = { viewModel.clearPlaybackHistory() }) {
                        Text("Clear", color = NeonMagenta, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(end = 16.dp)
                ) {
                    items(recentHistory) { song ->
                        RecentSongCard(
                            song = song,
                            style = albumArtStyle,
                            isPlaying = isPlaying && (viewModel.currentPlayingSong.value?.title == song.title)
                        ) {
                            viewModel.playSong(song, recentHistory)
                        }
                    }
                }
            }
        }

        // --- 5. Top Chartbusters & Global Hits Shelf ---
        item {
            val displayedSongs = if (selectedGenre == "All") {
                discoverSongs
            } else {
                discoverSongs.filter {
                    it.genre.contains(selectedGenre, ignoreCase = true) ||
                    it.title.contains(selectedGenre, ignoreCase = true) ||
                    it.artist.contains(selectedGenre, ignoreCase = true)
                }
            }

            Text(
                text = if (selectedGenre == "All") "Top Chartbusters & Global Hits (${displayedSongs.size})" else "$selectedGenre Hits (${displayedSongs.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                displayedSongs.forEach { song ->
                    val isFav = favorites.any { it.title == song.title && it.artist == song.artist }
                    SongRow(
                        song = song,
                        isPlayingNow = viewModel.currentPlayingSong.value?.title == song.title,
                        isFavorite = isFav,
                        onFavoriteClick = { viewModel.toggleFavorite(song) },
                        style = albumArtStyle,
                        isPlaying = isPlaying,
                        onClick = {
                            viewModel.playSong(song, displayedSongs)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RecentSongCard(
    song: Song,
    style: SongViewModel.AlbumArtStyle = SongViewModel.AlbumArtStyle.AURA,
    isPlaying: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(100.dp)
            .clickable { onClick() }
    ) {
        PremiumAlbumArt(
            title = song.title,
            artist = song.artist,
            modifier = Modifier.size(100.dp),
            cornerRadius = 16,
            style = style,
            isPlaying = isPlaying
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = song.title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = song.artist,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SongRow(
    song: Song,
    isPlayingNow: Boolean,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    style: SongViewModel.AlbumArtStyle = SongViewModel.AlbumArtStyle.AURA,
    isPlaying: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isPlayingNow) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
            .clickable { onClick() }
            .padding(8.dp),
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like Song",
                    tint = if (isFavorite) NeonMagenta else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = song.durationText,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

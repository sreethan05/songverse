package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Song
import com.example.data.api.GeminiDetailedSongInfo
import com.example.data.database.RecentSearchEntity
import com.example.ui.components.PremiumAlbumArt
import com.example.ui.components.SyncedLyricsView
import com.example.ui.theme.*
import com.example.ui.viewmodel.SongViewModel

@Composable
fun MiniPlayer(
    viewModel: SongViewModel,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSong by viewModel.currentPlayingSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val progress by viewModel.playbackProgress.collectAsState()
    val albumArtStyle by viewModel.albumArtStyle.collectAsState()
    val volume by viewModel.volume.collectAsState()
    val dominantColor by viewModel.dominantColor.collectAsState()

    val animatedDominantColor by animateColorAsState(
        targetValue = dominantColor,
        animationSpec = tween(durationMillis = 800, easing = LinearOutSlowInEasing),
        label = "miniPlayerDominantColor"
    )

    if (currentSong == null) return

    val totalDuration = currentSong!!.durationSeconds

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurfaceElevated.copy(alpha = 0.95f))
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        animatedDominantColor.copy(alpha = 0.8f),
                        GlassBorder,
                        animatedDominantColor.copy(alpha = 0.4f)
                    )
                ),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .padding(top = 10.dp, bottom = 10.dp, start = 16.dp, end = 16.dp)
    ) {
        // Row 1: Metadata + Core Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Interactive Metadata Section (Clicking this expands to full player)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onExpand() }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Small Generative Album Cover
                PremiumAlbumArt(
                    title = currentSong!!.title,
                    artist = currentSong!!.artist,
                    modifier = Modifier.size(44.dp),
                    cornerRadius = 8,
                    style = albumArtStyle,
                    isPlaying = isPlaying
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Title, Artist and Expand Indicator
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentSong!!.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = currentSong!!.artist,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Expand Player",
                            tint = NeonCyan.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Quick actions on the right (Like, Play/Pause, Next)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { viewModel.toggleFavorite(currentSong!!) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (currentSong!!.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (currentSong!!.isFavorite) NeonMagenta else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.togglePlayPause() },
                    modifier = Modifier
                        .testTag("mini_player_play_pause")
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(NeonCyan)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = DarkBg,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.skipNext() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Skip Next",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Row 2: Seek Slider + Volume Control
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Seek Time Elapsed
            Text(
                text = formatTime(progress),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary.copy(alpha = 0.8f),
                modifier = Modifier.width(30.dp),
                textAlign = TextAlign.Start
            )

            // Seek Slider
            Slider(
                value = progress.toFloat(),
                onValueChange = { viewModel.seekTo(it.toInt()) },
                valueRange = 0f..totalDuration.toFloat().coerceAtLeast(1f),
                colors = SliderDefaults.colors(
                    thumbColor = NeonCyan,
                    activeTrackColor = NeonCyan,
                    inactiveTrackColor = TextSecondary.withAlpha(0.15f)
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(24.dp)
                    .testTag("mini_player_seek_slider")
            )

            // Seek Time Remaining
            Text(
                text = formatTime(totalDuration),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary.copy(alpha = 0.8f),
                modifier = Modifier.width(30.dp),
                textAlign = TextAlign.End
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Volume Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Volume Icon
                IconButton(
                    onClick = {
                        if (volume > 0f) {
                            viewModel.setVolume(0f)
                        } else {
                            viewModel.setVolume(0.5f)
                        }
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (volume == 0f) Icons.Default.VolumeMute else if (volume < 0.5f) Icons.Default.VolumeDown else Icons.Default.VolumeUp,
                        contentDescription = "Mute",
                        tint = TextSecondary.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Volume Slider
                Slider(
                    value = volume,
                    onValueChange = { viewModel.setVolume(it) },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonPurple,
                        activeTrackColor = NeonPurple,
                        inactiveTrackColor = TextSecondary.withAlpha(0.15f)
                    ),
                    modifier = Modifier
                        .width(72.dp)
                        .height(24.dp)
                        .testTag("mini_player_volume_slider")
                )
            }
        }
    }
}

@Composable
fun FullPlayerScreen(
    viewModel: SongViewModel,
    onMinimize: () -> Unit,
    onSearchQuerySelect: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentSong by viewModel.currentPlayingSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val progress by viewModel.playbackProgress.collectAsState()
    val isShuffle by viewModel.isShuffleEnabled.collectAsState()
    val isRepeat by viewModel.isRepeatEnabled.collectAsState()
    val albumArtStyle by viewModel.albumArtStyle.collectAsState()
    val dominantColor by viewModel.dominantColor.collectAsState()
    val extractedSwatches by viewModel.extractedSwatches.collectAsState()

    val animatedDominantColor by animateColorAsState(
        targetValue = dominantColor,
        animationSpec = tween(durationMillis = 800, easing = LinearOutSlowInEasing),
        label = "animatedDominantColor"
    )

    val detailedInfo by viewModel.detailedSongInfo.collectAsState()
    val isDetailedLoading by viewModel.isDetailedInfoLoading.collectAsState()
    var isSidebarOpen by remember { mutableStateOf(false) }

    if (isSidebarOpen) {
        BackHandler {
            isSidebarOpen = false
        }
    }

    if (currentSong == null) return

    val scrollState = rememberScrollState()

    // Smooth cover-art pulse animation when playing
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by if (isPlaying) {
        infiniteTransition.animateFloat(
            initialValue = 0.98f,
            targetValue = 1.04f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        animatedDominantColor.copy(alpha = 0.55f),
                        animatedDominantColor.copy(alpha = 0.18f),
                        DarkBg
                    )
                )
            )
    ) {
        // Ambient radial background glow extracted via Coil from album art
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        Brush.radialGradient(
                            colors = listOf(
                                animatedDominantColor.copy(alpha = 0.45f * currentSong!!.energy),
                                Color.Transparent
                            ),
                            center = Offset(this.size.width / 2f, this.size.height / 3f),
                            radius = this.size.width * 0.9f
                        )
                    )
                }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            // --- 1. Top Header Navigation Bar ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onMinimize) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Minimize Player",
                        tint = TextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NOW PLAYING",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = currentSong!!.genre.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { isSidebarOpen = !isSidebarOpen },
                        modifier = Modifier.testTag("toggle_sidebar_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Song Backstory and Bio",
                            tint = if (isSidebarOpen) NeonMagenta else NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // --- Scrollable Center Content ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Spacer
                Spacer(modifier = Modifier.height(16.dp))

                // --- 2. Dynamic Pulsing Album Cover ---
                Box(
                    modifier = Modifier
                        .size((240f * pulseScale).dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            Brush.sweepGradient(
                                colors = listOf(
                                    animatedDominantColor,
                                    animatedDominantColor.copy(alpha = 0.5f),
                                    NeonCyan,
                                    animatedDominantColor
                                )
                            )
                        )
                        .padding(3.dp)
                        .clip(RoundedCornerShape(29.dp))
                        .background(DarkSurface),
                    contentAlignment = Alignment.Center
                ) {
                    PremiumAlbumArt(
                        title = currentSong!!.title,
                        artist = currentSong!!.artist,
                        modifier = Modifier.fillMaxSize(),
                        cornerRadius = 29,
                        style = albumArtStyle,
                        isPlaying = isPlaying
                    )
                }

                // --- Coil Extracted Dominant Color Swatch Indicator ---
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurfaceElevated.copy(alpha = 0.6f))
                        .border(1.dp, animatedDominantColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(animatedDominantColor)
                    )
                    Text(
                        text = "Coil Dynamic Palette",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    if (extractedSwatches.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            extractedSwatches.take(4).forEach { swatchColor ->
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(swatchColor)
                                )
                            }
                        }
                    }
                }

                // --- Album Art Style Selector ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurfaceElevated.copy(alpha = 0.5f))
                        .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val stylesList = listOf(
                        Triple(SongViewModel.AlbumArtStyle.AURA, "Aura", "✨"),
                        Triple(SongViewModel.AlbumArtStyle.VINYL, "Vinyl", "💿"),
                        Triple(SongViewModel.AlbumArtStyle.CYBERPUNK, "Cyber", "🌆"),
                        Triple(SongViewModel.AlbumArtStyle.MINIMAL, "Minimal", "🔳")
                    )
                    stylesList.forEach { (artStyle, label, iconStr) ->
                        val isSelected = albumArtStyle == artStyle
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) DarkSurface else Color.Transparent)
                                .then(
                                    if (isSelected) {
                                        Modifier.border(
                                            width = 1.dp,
                                            brush = Brush.linearGradient(listOf(NeonCyan, NeonPurple)),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    } else Modifier
                                )
                                .clickable { viewModel.setAlbumArtStyle(artStyle) }
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = iconStr, fontSize = 12.sp)
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) NeonCyan else TextSecondary
                                )
                            }
                        }
                    }
                }

                // --- 3. Live Canvas Wave Music Visualizer & Controls Panel ---
                var visualizerStyle by remember { mutableStateOf(VisualizerStyle.PARTICLE_WAVES) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    DarkSurface.copy(alpha = 0.6f),
                                    DarkSurfaceElevated.copy(alpha = 0.3f)
                                )
                            )
                        )
                        .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Audio Visualizer Section Header
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
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "AUDIO SPECTRUM DECK",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                letterSpacing = 1.2.sp
                            )
                        }

                        // Compact Glowing Status Indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isPlaying) NeonCyan else Color.Gray.copy(alpha = 0.6f))
                            )
                            Text(
                                text = if (isPlaying) "LIVE" else "PAUSED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isPlaying) NeonCyan else TextSecondary,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    MusicVisualizer(
                        isPlaying = isPlaying,
                        energy = currentSong!!.energy,
                        tempoBpm = currentSong!!.tempoBpm,
                        style = visualizerStyle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )

                    // Compact premium style selector (Controls Panel)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkBg.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                            .border(0.5.dp, GlassBorder.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VisualizerStyle.values().forEach { style ->
                            val isSelected = style == visualizerStyle
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) {
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    NeonPurple.copy(alpha = 0.35f),
                                                    NeonCyan.copy(alpha = 0.15f)
                                                )
                                            )
                                        } else {
                                            Brush.horizontalGradient(
                                                colors = listOf(Color.Transparent, Color.Transparent)
                                            )
                                        }
                                    )
                                    .border(
                                        width = 1.dp,
                                        brush = if (isSelected) {
                                            Brush.horizontalGradient(colors = listOf(NeonPurple, NeonCyan))
                                        } else {
                                            Brush.horizontalGradient(colors = listOf(Color.Transparent, Color.Transparent))
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { visualizerStyle = style }
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (style) {
                                        VisualizerStyle.FREQUENCY_BARS -> Icons.Default.BarChart
                                        VisualizerStyle.PARTICLE_WAVES -> Icons.Default.BubbleChart
                                        VisualizerStyle.ORBITAL_RINGS -> Icons.Default.Adjust
                                    },
                                    contentDescription = null,
                                    tint = if (isSelected) NeonCyan else TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = style.displayName,
                                    color = if (isSelected) NeonCyan else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // --- 4. Title and Artist Info ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentSong!!.title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${currentSong!!.artist} • ${currentSong!!.album} (${currentSong!!.releaseYear})",
                                fontSize = 15.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (currentSong!!.source == "JioSaavn" || currentSong!!.audioUrl.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF1DB954).copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1DB954).copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF1DB954))
                                        )
                                        Text(
                                            text = "JioSaavn 320kbps",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1DB954)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    IconButton(onClick = { viewModel.toggleFavorite(currentSong!!) }) {
                        Icon(
                            imageVector = if (currentSong!!.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like Song",
                            tint = if (currentSong!!.isFavorite) NeonMagenta else TextSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // --- 5. Timeline Seek Slider ---
                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = progress.toFloat(),
                        onValueChange = { viewModel.seekTo(it.toInt()) },
                        valueRange = 0f..(currentSong!!.durationSeconds.toFloat()),
                        colors = SliderDefaults.colors(
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = Color.Black.copy(alpha = 0.08f),
                            thumbColor = NeonCyan
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(progress),
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = formatTime(currentSong!!.durationSeconds),
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                // --- 6. Synced scrolling Lyrics Display ---
                SyncedLyricsView(
                    lyricsText = currentSong!!.lyrics,
                    durationSeconds = currentSong!!.durationSeconds,
                    playbackProgressSeconds = progress,
                    onSeekTo = { viewModel.seekTo(it) },
                    modifier = Modifier.fillMaxWidth()
                )

                // --- 7. Behind-The-Scenes Song Story/Trivia Card (AI Generated) ---
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GlassBorder, RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = NeonMagenta,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "AI Beat Trivia",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Text(
                            text = currentSong!!.trivia,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // --- 8. Core Playback Controls Panel (Pinned Bottom) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shuffle Button
                    IconButton(onClick = { viewModel.toggleShuffle() }) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (isShuffle) NeonCyan else TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Skip Previous
                    IconButton(onClick = { viewModel.skipPrevious() }) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous Track",
                            tint = TextPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Play / Pause Circle
                    IconButton(
                        onClick = { viewModel.togglePlayPause() },
                        modifier = Modifier
                            .testTag("full_player_play_pause")
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(NeonPurple, NeonCyan)
                                )
                            )
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Skip Next
                    IconButton(onClick = { viewModel.skipNext() }) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next Track",
                            tint = TextPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Repeat Button
                    IconButton(onClick = { viewModel.toggleRepeat() }) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = "Repeat",
                            tint = if (isRepeat) NeonCyan else TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Volume Control Slider Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val volume by viewModel.volume.collectAsState()
                    val isMuted = volume == 0f

                    IconButton(
                        onClick = {
                            if (isMuted) {
                                viewModel.setVolume(0.3f)
                            } else {
                                viewModel.setVolume(0f)
                            }
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeMute else if (volume < 0.5f) Icons.Default.VolumeDown else Icons.Default.VolumeUp,
                            contentDescription = "Mute/Unmute",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Slider(
                        value = volume,
                        onValueChange = { viewModel.setVolume(it) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("volume_slider"),
                        colors = SliderDefaults.colors(
                            activeTrackColor = NeonPurple,
                            inactiveTrackColor = Color.Black.copy(alpha = 0.08f),
                            thumbColor = NeonPurple
                        )
                    )

                    Text(
                        text = "${(volume * 100).toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.width(32.dp),
                        textAlign = TextAlign.End
                    )
                }
            }
        }

        // 9. Collapsible Sidebar for Stories, Artist Bios, and Release Info
        SongDetailsSidebar(
            isOpen = isSidebarOpen,
            onClose = { isSidebarOpen = false },
            song = currentSong!!,
            detailedInfo = detailedInfo,
            isLoading = isDetailedLoading,
            viewModel = viewModel,
            onSearchQuerySelect = onSearchQuerySelect
        )
    }
}

@Composable
fun SongDetailsSidebar(
    isOpen: Boolean,
    onClose: () -> Unit,
    song: Song,
    detailedInfo: GeminiDetailedSongInfo?,
    isLoading: Boolean,
    viewModel: SongViewModel,
    onSearchQuerySelect: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedSidebarTab by remember { mutableStateOf(0) } // 0: Behind Beat, 1: Play Queue, 2: Search History
    val recentSearches by viewModel.recentSearches.collectAsState()
    val playbackQueue by viewModel.playbackQueueState.collectAsState()
    val currentQueueIndex by viewModel.currentQueueIndexState.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Semi-transparent Scrim with Fade Transition
        AnimatedVisibility(
            visible = isOpen,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable(onClick = onClose)
            )
        }

        // 2. Sliding Sidebar Content Panel with Slide-in Transition from Right
        AnimatedVisibility(
            visible = isOpen,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(400, easing = EaseOutQuart)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(350, easing = EaseInCubic)
            ),
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.85f)
                .align(Alignment.CenterEnd)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("sidebar_container")
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(listOf(GlassBorder, Color.Transparent)),
                        shape = RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    // Top Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = when (selectedSidebarTab) {
                                    0 -> Icons.Default.AutoAwesome
                                    1 -> Icons.Default.QueueMusic
                                    else -> Icons.Default.History
                                },
                                contentDescription = null,
                                tint = NeonMagenta,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = when (selectedSidebarTab) {
                                    0 -> "Behind The Beat"
                                    1 -> "Play Queue"
                                    else -> "Recent Searches"
                                },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                        }

                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.testTag("close_sidebar_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Sidebar",
                                tint = TextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Segmented Custom Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1.5f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedSidebarTab == 0) NeonCyan.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable { selectedSidebarTab = 0 }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Behind Beat",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedSidebarTab == 0) NeonCyan else TextSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1.3f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedSidebarTab == 1) NeonCyan.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable { selectedSidebarTab = 1 }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Up Next",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedSidebarTab == 1) NeonCyan else TextSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1.2f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedSidebarTab == 2) NeonCyan.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable { selectedSidebarTab = 2 }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "History",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedSidebarTab == 2) NeonCyan else TextSecondary
                            )
                        }
                    }

                    // Content Area
                    if (selectedSidebarTab == 1) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Up Next (${playbackQueue.size})",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                if (playbackQueue.isNotEmpty()) {
                                    TextButton(
                                        onClick = { viewModel.clearQueue() },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text(
                                            text = "Clear Queue",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NeonMagenta
                                        )
                                    }
                                }
                            }

                            if (playbackQueue.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.padding(horizontal = 24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.QueueMusic,
                                            contentDescription = null,
                                            tint = TextSecondary.copy(alpha = 0.5f),
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Text(
                                            text = "Your Queue is Empty",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Add tracks from the Discover, Search, or Library tab to fill your playback stream.",
                                            fontSize = 12.sp,
                                            color = TextSecondary,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(playbackQueue.size) { index ->
                                        val item = playbackQueue[index]
                                        val isCurrent = index == currentQueueIndex
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isCurrent) NeonCyan.copy(alpha = 0.4f) else GlassBorder,
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .clickable {
                                                    viewModel.playQueueSong(index)
                                                },
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isCurrent) NeonCyan.copy(alpha = 0.08f) else DarkSurface.copy(alpha = 0.4f)
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    if (isCurrent) {
                                                        Icon(
                                                            imageVector = Icons.Default.PlayArrow,
                                                            contentDescription = "Playing",
                                                            tint = NeonCyan,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    } else {
                                                        Text(
                                                            text = "${index + 1}",
                                                            fontSize = 11.sp,
                                                            color = TextSecondary,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.width(16.dp),
                                                            textAlign = TextAlign.Center
                                                        )
                                                    }

                                                    Column {
                                                        Text(
                                                            text = item.title,
                                                            fontSize = 13.sp,
                                                            color = if (isCurrent) NeonCyan else TextPrimary,
                                                            fontWeight = FontWeight.Bold,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Text(
                                                            text = item.artist,
                                                            fontSize = 11.sp,
                                                            color = TextSecondary,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    if (index > 0) {
                                                        IconButton(
                                                            onClick = { viewModel.moveQueueItem(index, index - 1) },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.ArrowUpward,
                                                                contentDescription = "Move Up",
                                                                tint = TextSecondary,
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                        }
                                                    }
                                                    if (index < playbackQueue.size - 1) {
                                                        IconButton(
                                                            onClick = { viewModel.moveQueueItem(index, index + 1) },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.ArrowDownward,
                                                                contentDescription = "Move Down",
                                                                tint = TextSecondary,
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                        }
                                                    }

                                                    IconButton(
                                                        onClick = { viewModel.removeSongFromQueue(index) },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Close,
                                                            contentDescription = "Remove From Queue",
                                                            tint = TextSecondary,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (selectedSidebarTab == 2) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Past Searches",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                if (recentSearches.isNotEmpty()) {
                                    TextButton(
                                        onClick = { viewModel.clearRecentSearches() },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text(
                                            text = "Clear All",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NeonMagenta
                                        )
                                    }
                                }
                            }

                            if (recentSearches.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.padding(horizontal = 24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = null,
                                            tint = TextSecondary.copy(alpha = 0.5f),
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Text(
                                            text = "No Search History",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Your past searches will be stored here for quick one-tap access.",
                                            fontSize = 12.sp,
                                            color = TextSecondary,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(recentSearches.size) { index ->
                                        val item = recentSearches[index]
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(
                                                    width = 1.dp,
                                                    color = GlassBorder,
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .clickable {
                                                    onSearchQuerySelect(item.query)
                                                },
                                            colors = CardDefaults.cardColors(
                                                containerColor = DarkSurface.copy(alpha = 0.4f)
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Search,
                                                        contentDescription = null,
                                                        tint = NeonCyan,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Text(
                                                        text = item.query,
                                                        fontSize = 13.sp,
                                                        color = TextPrimary,
                                                        fontWeight = FontWeight.Medium,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                IconButton(
                                                    onClick = {
                                                        viewModel.removeRecentSearch(item.query)
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Remove Search Query",
                                                        tint = TextSecondary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Original Behind The Beat Content
                        if (isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    CircularProgressIndicator(
                                        color = NeonCyan,
                                        strokeWidth = 3.dp,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        text = "Analyzing song with AI...",
                                        fontSize = 13.sp,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        } else if (detailedInfo != null) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                // Song Header Card
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, GlassBorder, RoundedCornerShape(16.dp)),
                                    colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    Brush.linearGradient(listOf(NeonPurple, NeonCyan))
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MusicNote,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = song.title,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = song.artist,
                                                fontSize = 13.sp,
                                                color = NeonCyan,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                // 1. Detailed Story Section
                                SidebarSectionCard(
                                    title = "Dynamic Backstory",
                                    icon = Icons.Default.MenuBook,
                                    contentColor = NeonCyan,
                                    text = detailedInfo.detailedBackstory
                                )

                                // 2. Artist Bio Section
                                SidebarSectionCard(
                                    title = "Artist Biography",
                                    icon = Icons.Default.Person,
                                    contentColor = NeonMagenta,
                                    text = detailedInfo.artistBio
                                )

                                // 3. Release & Credits Section
                                SidebarSectionCard(
                                    title = "Release & Reception",
                                    icon = Icons.Default.Info,
                                    contentColor = NeonPurple,
                                    text = detailedInfo.releaseDetails
                                )

                                // 4. Track Analytics Section
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, GlassBorder, RoundedCornerShape(16.dp)),
                                    colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.BarChart,
                                                contentDescription = null,
                                                tint = NeonCyan,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "Audio Analytics",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            AnalyticsBadge(label = "BPM", value = "${song.tempoBpm}", color = NeonCyan)
                                            AnalyticsBadge(label = "Mood", value = song.mood, color = NeonMagenta)
                                            AnalyticsBadge(label = "Energy", value = String.format("%.0f%%", song.energy * 100), color = NeonPurple)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SidebarSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentColor: Color,
    text: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Text(
                text = text,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = TextSecondary,
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
private fun AnalyticsBadge(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.1f))
            .border(0.5.dp, color.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = label.uppercase(),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
    }
}

enum class VisualizerStyle(val displayName: String) {
    FREQUENCY_BARS("Bars"),
    PARTICLE_WAVES("Particles"),
    ORBITAL_RINGS("Orbital")
}

private fun lerpColor(start: Color, end: Color, fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return Color(
        red = start.red + (end.red - start.red) * f,
        green = start.green + (end.green - start.green) * f,
        blue = start.blue + (end.blue - start.blue) * f,
        alpha = start.alpha + (end.alpha - start.alpha) * f
    )
}

@Composable
fun MusicVisualizer(
    isPlaying: Boolean,
    energy: Float,
    tempoBpm: Int,
    style: VisualizerStyle,
    modifier: Modifier = Modifier
) {
    // Generate an oscillating phase offset when playing or paused (slow standby drift)
    val transition = rememberInfiniteTransition(label = "phase_loop")
    val cycleDuration = if (isPlaying) {
        (60000 / tempoBpm).coerceIn(400, 2000)
    } else {
        8000 // Slow elegant standby breathing when paused
    }
    
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(cycleDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val visualizerEnergy = if (isPlaying) energy else 0.18f

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val centerX = width / 2f
        val energy = visualizerEnergy // Standby override to keep lines breathing beautifully when paused

        when (style) {
            VisualizerStyle.FREQUENCY_BARS -> {
                val barCount = 18
                val spacing = 6.dp.toPx()
                val barWidth = (width - (spacing * (barCount - 1))) / barCount
                for (i in 0 until barCount) {
                    val angle = (i.toFloat() / barCount) * (3 * Math.PI).toFloat() - phase * 1.5f
                    val heightMultiplier = (0.2f + 0.8f * Math.abs(Math.sin(angle.toDouble()).toFloat())) * energy
                    val barHeight = (height * 0.85f * heightMultiplier).coerceIn(4.dp.toPx(), height * 0.95f)

                    val fraction = i.toFloat() / (barCount - 1)
                    val color = when {
                        fraction < 0.5f -> lerpColor(NeonCyan, NeonPurple, fraction * 2f)
                        else -> lerpColor(NeonPurple, NeonMagenta, (fraction - 0.5f) * 2f)
                    }

                    val x = i * (barWidth + spacing)
                    val y = centerY - barHeight / 2f

                    val gradient = Brush.verticalGradient(
                        colors = listOf(color, color.copy(alpha = 0.25f)),
                        startY = y,
                        endY = y + barHeight
                    )

                    drawRoundRect(
                        brush = gradient,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            }
            VisualizerStyle.PARTICLE_WAVES -> {
                // 3 layered wavy paths for complex cinematic ocean feel
                val waveSpecs = listOf(
                    Triple(NeonCyan.copy(alpha = 0.35f), 1.0f, 0f),
                    Triple(NeonPurple.copy(alpha = 0.25f), 0.7f, Math.PI.toFloat() * 0.5f),
                    Triple(NeonMagenta.copy(alpha = 0.15f), 1.3f, Math.PI.toFloat())
                )
                
                waveSpecs.forEach { (color, scale, phaseShift) ->
                    val path = Path()
                    path.moveTo(0f, centerY)
                    val pointsCount = 60
                    for (i in 0..pointsCount) {
                        val x = (i.toFloat() / pointsCount) * width
                        val angle = (i.toFloat() / pointsCount) * (2 * Math.PI).toFloat() * 1.5f - phase + phaseShift
                        val sineVal = Math.sin(angle.toDouble()).toFloat() + 0.2f * Math.sin((angle * 2.5).toDouble()).toFloat()
                        val y = centerY + sineVal * (height * 0.25f * energy * scale)
                        path.lineTo(x, y)
                    }
                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                }

                // Bright floating neon particles floating on the main wave path
                val particleCount = 10
                for (i in 0 until particleCount) {
                    val xFraction = i.toFloat() / (particleCount - 1)
                    val x = xFraction * width
                    val angle = xFraction * (2 * Math.PI).toFloat() * 1.5f - phase
                    val sineVal = Math.sin(angle.toDouble()).toFloat() + 0.2f * Math.sin((angle * 2.5).toDouble()).toFloat()
                    val y = centerY + sineVal * (height * 0.25f * energy)
                    val color = lerpColor(NeonCyan, NeonPurple, xFraction)
                    
                    val radius = (5.dp.toPx() + 2.dp.toPx() * energy)
                    // outer glow shadow
                    drawCircle(
                        color = color.copy(alpha = 0.35f),
                        radius = radius * 2.2f,
                        center = Offset(x, y)
                    )
                    // core white speck for brilliance
                    drawCircle(
                        color = Color.White,
                        radius = radius * 0.4f,
                        center = Offset(x, y)
                    )
                    // neon ring
                    drawCircle(
                        color = color,
                        radius = radius,
                        center = Offset(x, y),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
            }
            VisualizerStyle.ORBITAL_RINGS -> {
                // Central Glowing Energy Core Orb
                val coreRadius = (12.dp.toPx() + 8.dp.toPx() * energy)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(NeonPurple.copy(alpha = 0.45f * energy), Color.Transparent),
                        center = Offset(centerX, centerY),
                        radius = coreRadius * 2.5f
                    ),
                    radius = coreRadius * 2.5f,
                    center = Offset(centerX, centerY)
                )
                drawCircle(
                    color = NeonCyan.copy(alpha = 0.8f),
                    radius = coreRadius,
                    center = Offset(centerX, centerY)
                )
                drawCircle(
                    color = Color.White,
                    radius = coreRadius * 0.4f,
                    center = Offset(centerX, centerY)
                )

                // Animated Telemetry Rings
                val ringCount = 3
                val maxDiameter = Math.min(width, height)
                for (i in 0 until ringCount) {
                    val ringPhase = (phase / (2 * Math.PI).toFloat() + i.toFloat() / ringCount) % 1f
                    val baseRadius = maxDiameter * 0.15f
                    val maxRadius = maxDiameter * 0.48f
                    val radius = baseRadius + (maxRadius - baseRadius) * ringPhase
                    val opacity = (1f - ringPhase) * 0.6f * energy

                    val color = when (i) {
                        0 -> NeonCyan
                        1 -> NeonPurple
                        else -> NeonMagenta
                    }

                    // Orbital trajectory ring
                    drawCircle(
                        color = color.copy(alpha = opacity),
                        radius = radius,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = (1.5.dp.toPx() + 1.5.dp.toPx() * energy))
                    )

                    // Glowing satellite planet node orbiting on this ring
                    val satelliteAngle = phase * 1.5f + i * (Math.PI.toFloat() * 2 / ringCount)
                    val satX = centerX + radius * Math.cos(satelliteAngle.toDouble()).toFloat()
                    val satY = centerY + radius * Math.sin(satelliteAngle.toDouble()).toFloat()

                    drawCircle(
                        color = color.copy(alpha = opacity + 0.35f),
                        radius = (5.dp.toPx() + 2.dp.toPx() * energy),
                        center = Offset(satX, satY)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = opacity + 0.6f),
                        radius = 2.dp.toPx(),
                        center = Offset(satX, satY)
                    )
                }
            }
        }
    }
}

// --- Format Helpers ---

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%d:%02d", m, s)
}

// Float extension to withAlpha replacement since withAlpha is standard inside Compose color now
private fun Color.withAlpha(alpha: Float): Color {
    return this.copy(alpha = alpha)
}



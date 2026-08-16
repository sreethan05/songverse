package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

// --- Synced Lyrics Data Class ---
data class LyricLine(
    val text: String,
    val startTimeSeconds: Float,
    val endTimeSeconds: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncedLyricsView(
    lyricsText: String,
    durationSeconds: Int,
    playbackProgressSeconds: Int,
    onSeekTo: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. Parse Lyrics
    val parsedLyrics = remember(lyricsText, durationSeconds) {
        parseLyrics(lyricsText, durationSeconds)
    }

    // 2. Local Customization States (Text Size & Synchronization Offset)
    var lyricTextSize by remember { mutableStateOf(16f) }
    var timeOffsetSeconds by remember { mutableStateOf(0f) } // Adjust sync offset
    
    // 3. Search states
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    // 4. Calculate current playing progress adjusted by user's time offset
    val adjustedProgress = remember(playbackProgressSeconds, timeOffsetSeconds) {
        (playbackProgressSeconds + timeOffsetSeconds).coerceAtLeast(0f)
    }

    // 5. Detect active lyric line
    val activeLineIndex = remember(parsedLyrics, adjustedProgress) {
        val foundIndex = parsedLyrics.indexOfFirst { adjustedProgress >= it.startTimeSeconds && adjustedProgress < it.endTimeSeconds }
        if (foundIndex != -1) foundIndex else {
            // Find the closest line if current time exceeds all
            if (parsedLyrics.isNotEmpty() && adjustedProgress >= parsedLyrics.last().endTimeSeconds) {
                parsedLyrics.lastIndex
            } else 0
        }
    }

    // 6. Manage Auto-Scrolling
    val lazyListState = rememberLazyListState()
    LaunchedEffect(activeLineIndex, isSearching) {
        if (!isSearching && parsedLyrics.isNotEmpty() && activeLineIndex >= 0) {
            try {
                lazyListState.animateScrollToItem(
                    index = activeLineIndex,
                    scrollOffset = -80
                )
            } catch (e: Exception) {
                // Safeguard against scroll exceptions
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, GlassBorder, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.65f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // --- Lyrics Top Header Panel ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        contentDescription = "Synced Lyrics Icon",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Live Synced Lyrics",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                // Interactive Controls Row (Text Scale, Offset Adjust, Search Trigger)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Search toggle button
                    IconButton(
                        onClick = {
                            isSearching = !isSearching
                            if (!isSearching) searchQuery = ""
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isSearching) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search Lyrics",
                            tint = if (isSearching) NeonMagenta else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Text Decrement Size "A-"
                    IconButton(
                        onClick = { lyricTextSize = (lyricTextSize - 1.5f).coerceAtLeast(12f) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text(
                            text = "A-",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    }

                    // Text Increment Size "A+"
                    IconButton(
                        onClick = { lyricTextSize = (lyricTextSize + 1.5f).coerceAtMost(24f) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text(
                            text = "A+",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    }

                    // Timing offset indicators
                    Surface(
                        color = NeonPurple.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .clickable {
                                // Cycle offsets: 0s -> +1.5s -> -1.5s -> 0s
                                timeOffsetSeconds = when (timeOffsetSeconds) {
                                    0f -> 1.5f
                                    1.5f -> -1.5f
                                    else -> 0f
                                }
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Sync Offset",
                                tint = NeonCyan,
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text = if (timeOffsetSeconds == 0f) "Sync" else String.format("%+.1fs", timeOffsetSeconds),
                                color = NeonCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // --- Animated Search Bar ---
            AnimatedVisibility(visible = isSearching) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search lyrics...", fontSize = 12.sp, color = TextSecondary.copy(alpha = 0.6f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = GlassBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Filter lyrics if searching
            val filteredLines = remember(parsedLyrics, searchQuery) {
                if (searchQuery.isBlank()) {
                    parsedLyrics
                } else {
                    parsedLyrics.filter { it.text.contains(searchQuery, ignoreCase = true) }
                }
            }

            // --- Lyrics Box Container ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                if (filteredLines.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No matching lyrics found" else "Instrumental / No Lyrics",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    LazyColumn(
                        state = lazyListState,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(filteredLines) { idx, line ->
                            val originalIdx = parsedLyrics.indexOf(line)
                            val isActive = !isSearching && originalIdx == activeLineIndex

                            // Calculate character progression sweep inside current line
                            val lineProgress = if (isActive) {
                                val lineDuration = line.endTimeSeconds - line.startTimeSeconds
                                if (lineDuration > 0f) {
                                    ((adjustedProgress - line.startTimeSeconds) / lineDuration).coerceIn(0f, 1f)
                                } else 0f
                            } else 0f

                            // Smooth animation for scaling and opacities
                            val lyricSize by animateFloatAsState(
                                targetValue = if (isActive) lyricTextSize + 3.5f else lyricTextSize,
                                animationSpec = tween(350, easing = EaseOutQuart),
                                label = "size"
                            )
                            val lyricAlpha by animateFloatAsState(
                                targetValue = if (isActive) 1.0f else if (isSearching) 0.8f else 0.45f,
                                animationSpec = tween(350, easing = EaseOutQuart),
                                label = "alpha"
                            )
                            val glowRadius by animateFloatAsState(
                                targetValue = if (isActive) 12f else 0f,
                                animationSpec = tween(350, easing = EaseOutQuart),
                                label = "glow"
                            )

                            // Highlight sweep text brush
                            val textBrush = if (isActive) {
                                val p = lineProgress.coerceIn(0.001f, 0.999f)
                                Brush.horizontalGradient(
                                    colorStops = arrayOf(
                                        0.0f to NeonCyan,
                                        p to NeonCyan,
                                        p to TextPrimary,
                                        1.0f to TextPrimary
                                    )
                                )
                            } else {
                                Brush.horizontalGradient(listOf(TextPrimary, TextPrimary))
                            }

                            BoxWithConstraints(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        // Seek to the selected line's starting point
                                        onSeekTo(line.startTimeSeconds.toInt())
                                    }
                                    .background(
                                        if (isActive) DarkSurfaceElevated.copy(alpha = 0.45f)
                                        else Color.Transparent
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isActive) GlassBorder.copy(alpha = 0.4f) else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(vertical = 10.dp, horizontal = 12.dp)
                            ) {
                                // Add a subtle visual glow background sweeping bar behind active lyric
                                if (isActive) {
                                    val containerWidth = maxWidth
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(containerWidth * lineProgress)
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(
                                                        NeonCyan.copy(alpha = 0.1f),
                                                        NeonPurple.copy(alpha = 0.03f)
                                                    )
                                                )
                                            )
                                            .align(Alignment.CenterStart)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Play icon or Timestamp indicator
                                    Box(
                                        modifier = Modifier.width(36.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isActive) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Playing",
                                                tint = NeonCyan,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        } else {
                                            Text(
                                                text = formatTime(line.startTimeSeconds.toInt()),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextSecondary.copy(alpha = 0.4f)
                                            )
                                        }
                                    }

                                    // Dynamic Styled Text
                                    val currentStyle = if (isActive) {
                                        LocalTextStyle.current
                                            .copy(color = Color.Unspecified)
                                            .copy(
                                                brush = textBrush,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = lyricSize.sp,
                                                lineHeight = (lyricSize + 6).sp,
                                                shadow = if (glowRadius > 0f) {
                                                    Shadow(
                                                        color = NeonCyan.copy(alpha = 0.4f),
                                                        offset = Offset(0f, 0f),
                                                        blurRadius = glowRadius
                                                    )
                                                } else null
                                            )
                                    } else {
                                        LocalTextStyle.current.copy(
                                            color = TextPrimary.copy(alpha = lyricAlpha),
                                            fontWeight = FontWeight.Medium,
                                            fontSize = lyricSize.sp,
                                            lineHeight = (lyricSize + 6).sp
                                        )
                                    }

                                    Text(
                                        text = line.text,
                                        style = currentStyle,
                                        textAlign = TextAlign.Start,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Format helpers & parser ---

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%d:%02d", m, s)
}

fun parseLyrics(lyricsText: String, totalDurationSeconds: Int): List<LyricLine> {
    val lines = lyricsText.lines().map { it.trim() }
    
    // 1. Check if the lyrics have timestamp annotations, e.g., [00:12.34] or [00:12]
    val timestampRegex = """^\[(\d+):(\d+)(?:\.(\d+))?\](.*)$""".toRegex()
    val hasTimestamps = lines.any { timestampRegex.matches(it) }
    
    if (hasTimestamps) {
        val parsedLines = mutableListOf<LyricLine>()
        for (line in lines) {
            val match = timestampRegex.find(line)
            if (match != null) {
                val min = match.groupValues[1].toInt()
                val sec = match.groupValues[2].toInt()
                val ms = match.groupValues[3].toIntOrNull() ?: 0
                val text = match.groupValues[4].trim()
                
                val startTime = min * 60f + sec + (ms / 100f)
                parsedLines.add(LyricLine(text, startTime, 0f))
            } else if (line.isNotEmpty()) {
                val prevStart = parsedLines.lastOrNull()?.startTimeSeconds ?: 0f
                parsedLines.add(LyricLine(line, prevStart + 3f, 0f))
            }
        }
        
        // Fill end times for timestamped lyrics
        for (i in 0 until parsedLines.size) {
            val current = parsedLines[i]
            val nextStart = if (i < parsedLines.size - 1) {
                parsedLines[i + 1].startTimeSeconds
            } else {
                totalDurationSeconds.toFloat()
            }
            parsedLines[i] = current.copy(endTimeSeconds = nextStart)
        }
        return parsedLines
    }
    
    // 2. No timestamps: Automatically calculate proportional timing based on character density
    val singableLines = lines.filter { it.isNotEmpty() }
    if (singableLines.isEmpty()) {
        return listOf(LyricLine("Instrumental", 0f, totalDurationSeconds.toFloat()))
    }
    
    val leadIn = totalDurationSeconds * 0.05f // 5% lead-in
    val leadOut = totalDurationSeconds * 0.95f // 5% lead-out
    val activeDuration = leadOut - leadIn
    
    val totalChars = singableLines.sumOf { it.length }.toFloat()
    if (totalChars == 0f) {
        val lineDuration = activeDuration / singableLines.size
        return singableLines.mapIndexed { index, text ->
            LyricLine(
                text = text,
                startTimeSeconds = leadIn + index * lineDuration,
                endTimeSeconds = leadIn + (index + 1) * lineDuration
            )
        }
    }
    
    val result = mutableListOf<LyricLine>()
    var currentStart = leadIn
    
    for (text in singableLines) {
        val lineWeight = text.length / totalChars
        val duration = lineWeight * activeDuration
        val finalDuration = duration.coerceAtLeast(1.5f)
        val endTime = currentStart + finalDuration
        result.add(
            LyricLine(
                text = text,
                startTimeSeconds = currentStart,
                endTimeSeconds = endTime
            )
        )
        currentStart = endTime
    }
    
    // Adjust final line to end at the song's end
    if (result.isNotEmpty()) {
        val lastIndex = result.lastIndex
        result[lastIndex] = result[lastIndex].copy(endTimeSeconds = totalDurationSeconds.toFloat())
    }
    
    return result
}

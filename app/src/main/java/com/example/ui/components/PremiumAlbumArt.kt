package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.SongViewModel

@Composable
fun PremiumAlbumArt(
    title: String,
    artist: String,
    modifier: Modifier = Modifier,
    cornerRadius: Int = 12,
    showInitials: Boolean = true,
    style: SongViewModel.AlbumArtStyle = SongViewModel.AlbumArtStyle.AURA,
    isPlaying: Boolean = false
) {
    // Generate deterministic colors based on title and artist
    val hash = (title + artist).hashCode()
    val absHash = kotlin.math.abs(hash)
    
    val palettes = listOf(
        listOf(Color(0xFF00F5FF), Color(0xFF9D4EDD), Color(0xFFFF007F)), // Electric Cyan, Violet, Pink
        listOf(Color(0xFFFF007F), Color(0xFFFF9E00), Color(0xFF9D4EDD)), // Pink, Orange, Purple
        listOf(Color(0xFF7B2CBF), Color(0xFF00FF87), Color(0xFF00F5FF)), // Purple, Emerald, Cyan
        listOf(Color(0xFFFF3366), Color(0xFFFF9900), Color(0xFF7B2CBF)), // Crimson, Sunset, Violet
        listOf(Color(0xFF0055FF), Color(0xFF8A2BE2), Color(0xFFFF1493))  // Blue, Purple, DarkPink
    )
    
    val palette = palettes[absHash % palettes.size]
    val primaryColor = palette[0]
    val secondaryColor = palette[1]
    val tertiaryColor = palette[2]

    // Spinning animation for Vinyl Record
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_rotation")
    val rotationAngle by if (isPlaying && style == SongViewModel.AlbumArtStyle.VINYL) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(6000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    // Extraction of initials
    val cleanTitle = title.trim()
    val cleanArtist = artist.trim()
    val tLetter = cleanTitle.firstOrNull { it.isLetter() }?.uppercaseChar() ?: 'S'
    val aLetter = cleanArtist.firstOrNull { it.isLetter() }?.uppercaseChar() ?: 'V'
    val initialsText = "$tLetter$aLetter"

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius.dp))
            .background(Color(0xFF0A0A0E))
            .then(
                if (style == SongViewModel.AlbumArtStyle.MINIMAL) {
                    Modifier.border(
                        width = 1.5.dp,
                        color = Color(0xFFD4AF37).copy(alpha = 0.4f), // Elegant Gold Trim
                        shape = RoundedCornerShape(cornerRadius.dp)
                    )
                } else if (style == SongViewModel.AlbumArtStyle.CYBERPUNK) {
                    Modifier.border(
                        width = 1.dp,
                        brush = Brush.linearGradient(listOf(primaryColor, tertiaryColor)),
                        shape = RoundedCornerShape(cornerRadius.dp)
                    )
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(cornerRadius.dp)
                    )
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        when (style) {
            SongViewModel.AlbumArtStyle.AURA -> {
                // Style 1: Aura Fluid Mesh Gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(primaryColor.copy(alpha = 0.25f), Color(0xFF0E0E14))
                            )
                        )
                ) {
                    // Floating glowing spotlights on Canvas
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        
                        // Main organic glowing blobs
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(primaryColor.copy(alpha = 0.6f), Color.Transparent),
                                radius = w * 0.6f
                            ),
                            center = Offset(w * 0.3f, h * 0.3f),
                            radius = w * 0.6f
                        )
                        
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(secondaryColor.copy(alpha = 0.5f), Color.Transparent),
                                radius = w * 0.6f
                            ),
                            center = Offset(w * 0.8f, h * 0.7f),
                            radius = w * 0.6f
                        )

                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(tertiaryColor.copy(alpha = 0.35f), Color.Transparent),
                                radius = w * 0.45f
                            ),
                            center = Offset(w * 0.5f, h * 0.5f),
                            radius = w * 0.45f
                        )
                    }
                }
            }

            SongViewModel.AlbumArtStyle.VINYL -> {
                // Style 2: Retro Vinyl Player Disc
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF1C1B22), Color(0xFF0B0A0E))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Spinning vinyl record component
                    Box(
                        modifier = Modifier
                            .fillMaxSize(0.92f)
                            .rotate(rotationAngle)
                            .clip(CircleShape)
                            .background(Color(0xFF0D0D11)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val rMax = size.width / 2f
                            
                            // Draw the solid vinyl color
                            drawCircle(
                                color = Color(0xFF141419),
                                radius = rMax
                            )
                            
                            // Vinyl outer edge highlight
                            drawCircle(
                                color = Color.White.copy(alpha = 0.08f),
                                radius = rMax,
                                style = Stroke(width = 2.dp.toPx())
                            )
                            
                            // Fine vinyl sound groove lines
                            for (i in 1..8) {
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.03f * (10 - i) / 5f),
                                    radius = rMax * (0.35f + 0.07f * i),
                                    style = Stroke(width = 0.75.dp.toPx())
                                )
                            }
                            
                            // Glossy high-contrast vinyl sheen reflections
                            val sweepBrush = Brush.sweepGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.07f),
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.07f),
                                    Color.Transparent
                                )
                            )
                            drawCircle(
                                brush = sweepBrush,
                                radius = rMax * 0.95f
                            )
                        }

                        // Center sticker record label
                        Box(
                            modifier = Modifier
                                .fillMaxSize(0.38f)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(primaryColor, secondaryColor)
                                    )
                                )
                                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initialsText,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                style = androidx.compose.ui.text.TextStyle(
                                    shadow = androidx.compose.ui.graphics.Shadow(
                                        color = Color.Black.copy(alpha = 0.4f),
                                        blurRadius = 3f
                                    )
                                )
                            )
                            
                            // Spindle hole
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0A0A0E))
                                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                            )
                        }
                    }
                }
            }

            SongViewModel.AlbumArtStyle.CYBERPUNK -> {
                // Style 3: Vaporwave/Cyberpunk Perspective Grid
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF03001e), Color(0xFF7303c0), Color(0xFFec38bc))
                            )
                        )
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        
                        // Horizon position
                        val horizon = h * 0.55f
                        
                        // Drawing Glowing Retro Sun above horizon
                        drawArc(
                            color = Color(0xFFFF007F),
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = true,
                            topLeft = Offset(w * 0.25f, horizon - (w * 0.25f)),
                            size = Size(w * 0.5f, w * 0.5f)
                        )
                        
                        // Sun horizontal scan lines
                        for (i in 1..6) {
                            val lineY = horizon - (w * 0.04f * i)
                            if (lineY > horizon - (w * 0.25f)) {
                                drawLine(
                                    color = Color(0xFF03001e),
                                    start = Offset(w * 0.2f, lineY),
                                    end = Offset(w * 0.8f, lineY),
                                    strokeWidth = 2.dp.toPx()
                                )
                            }
                        }

                        // Perspective laser grid lines converging to center horizon
                        val gridHorizonX = w / 2f
                        val steps = 10
                        for (i in 0..steps) {
                            val startX = (w / steps) * i
                            drawLine(
                                color = Color(0xFF00F5FF).copy(alpha = 0.6f),
                                start = Offset(startX, h),
                                end = Offset(gridHorizonX, horizon),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                        
                        // Horizontal expanding grid lines
                        var currentY = horizon
                        var spacing = 4f
                        while (currentY < h) {
                            drawLine(
                                color = Color(0xFF00F5FF).copy(alpha = 0.4f),
                                start = Offset(0f, currentY),
                                end = Offset(w, currentY),
                                strokeWidth = 1.dp.toPx()
                            )
                            spacing *= 1.4f
                            currentY += spacing
                        }
                    }
                }
            }

            SongViewModel.AlbumArtStyle.MINIMAL -> {
                // Style 4: Minimalist Studio Slate
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF141416)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Very soft aesthetic background geometry
                        drawRect(
                            color = Color(0xFFD4AF37).copy(alpha = 0.03f),
                            size = size
                        )
                    }
                }
            }
        }

        // Deep glossy vignette overlay for three-dimensional premium feel
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.02f),
                            Color.Black.copy(alpha = 0.45f)
                        )
                    )
                )
        )

        // Title and Initials overlay (for styles other than VINYL or unless explicit)
        if (showInitials && style != SongViewModel.AlbumArtStyle.VINYL) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val sizePx = kotlin.math.min(maxWidth.value, maxHeight.value)
                val fontSizeSp = (sizePx * 0.32f).coerceAtLeast(10f).sp
                
                if (style == SongViewModel.AlbumArtStyle.MINIMAL) {
                    // Luxurious golden thin font
                    Text(
                        text = initialsText,
                        color = Color(0xFFD4AF37),
                        fontSize = fontSizeSp,
                        fontWeight = FontWeight.ExtraLight,
                        fontFamily = FontFamily.Serif,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    )
                } else {
                    // Thick neon shadow fonts
                    Text(
                        text = initialsText,
                        color = Color.White,
                        fontSize = fontSizeSp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = primaryColor.copy(alpha = 0.6f),
                                offset = Offset(0f, 2f),
                                blurRadius = 8f
                            )
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

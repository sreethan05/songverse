package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// --- Modern Spotify / Apple Music / YouTube Music Inspired Palette ---
val RawNeonCyan = Color(0xFF1DB954) // Spotify Emerald Green
val RawNeonMagenta = Color(0xFFFF3B30) // Apple Coral Red
val RawNeonPurple = Color(0xFF8E54E9) // Deep Vibrant Violet

var NeonCyan: Color = RawNeonCyan
var NeonPurple: Color = RawNeonPurple
var NeonMagenta: Color = RawNeonMagenta

val AccentOrange = Color(0xFFFF9500) // Vibrant Warm Orange
val AccentGreen = Color(0xFF1DB954) // Active Emerald Green
val ColorTextDark = Color(0xFFF3F4F6) // High contrast clean white text

// --- Dark Mode Static Palettes (Sleek Dark Obsidian) ---
val StaticDarkBg = Color(0xFF0F1117) // Pure dark obsidian void
val StaticDarkSurface = Color(0xFF181A20) // Premium sleek dark surface
val StaticDarkSurfaceElevated = Color(0xFF222630) // Glass elevated card surface
val StaticGlassBorder = Color(0x22FFFFFF) // Subtle 13% translucent glass border
val StaticTextPrimary = Color(0xFFF9FAFB) // High-contrast crisp white
val StaticTextSecondary = Color(0xFF9CA3AF) // Muted slate gray

// --- Light Mode Static Palettes (Modern Daylight Studio) ---
val StaticLightBg = Color(0xFFF8FAFC) // Crisp modern off-white
val StaticLightSurface = Color(0xFFFFFFFF) // Pure clean card white
val StaticLightSurfaceElevated = Color(0xFFF1F5F9) // Tinted light slate card
val StaticLightGlassBorder = Color(0xFFE2E8F0) // Subtle line border
val StaticLightTextPrimary = Color(0xFF0F172A) // Rich slate black for contrast
val StaticLightTextSecondary = Color(0xFF64748B) // Slate gray secondary text

// --- Dynamic @Composable mappings to avoid modifying screens! ---
val DarkBg: Color
    @Composable
    get() = MaterialTheme.colorScheme.background

val DarkSurface: Color
    @Composable
    get() = MaterialTheme.colorScheme.surface

val DarkSurfaceElevated: Color
    @Composable
    get() = MaterialTheme.colorScheme.surfaceVariant

val GlassBorder: Color
    @Composable
    get() = MaterialTheme.colorScheme.outline

val TextPrimary: Color
    @Composable
    get() = MaterialTheme.colorScheme.onBackground

val TextSecondary: Color
    @Composable
    get() = MaterialTheme.colorScheme.onSurfaceVariant



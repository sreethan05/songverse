package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppThemePreset {
    CYBERPUNK,
    OCEAN_BLUE,
    FOREST_EMERALD,
    SUNSET_GOLD
}

// 1. Emerald / Modern Spotify Style
private val DarkCyberpunkScheme = darkColorScheme(
    primary = Color(0xFF1DB954), // Spotify Green Accent
    secondary = Color(0xFF1ED760), // Luminous Green
    tertiary = Color(0xFFFF3B30), // Coral Red
    background = Color(0xFF0F1117), // Deep Obsidian Black
    surface = Color(0xFF181A20), // Clean Dark Card
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color(0xFFF9FAFB), // Crisp White
    onSurface = Color(0xFFF9FAFB),
    surfaceVariant = Color(0xFF222630), // Elevated Glass Card
    onSurfaceVariant = Color(0xFF9CA3AF), // Muted Slate Gray
    outline = Color(0x22FFFFFF) // Translucent Glass Border
)

private val LightCyberpunkScheme = lightColorScheme(
    primary = Color(0xFF1DB954),
    secondary = Color(0xFF059669),
    tertiary = Color(0xFFFF3B30),
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFFE2E8F0)
)

// 2. Deep Ocean Sapphire / Tidal Style
private val DarkOceanScheme = darkColorScheme(
    primary = Color(0xFF00E5FF), // Electric Cyan
    secondary = Color(0xFF3B82F6), // Sapphire Blue
    tertiary = Color(0xFF818CF8), // Indigo
    background = Color(0xFF0B1120),
    surface = Color(0xFF131C31),
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0x22FFFFFF)
)

private val LightOceanScheme = lightColorScheme(
    primary = Color(0xFF2563EB),
    secondary = Color(0xFF0284C7),
    tertiary = Color(0xFF0D9488),
    background = Color(0xFFF0F6FF),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1)
)

// 3. Electric Forest / Neon Mint
private val DarkForestScheme = darkColorScheme(
    primary = Color(0xFF10B981), // Emerald Mint
    secondary = Color(0xFF34D399),
    tertiary = Color(0xFF06B6D4),
    background = Color(0xFF09140E),
    surface = Color(0xFF112219),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color(0xFFF0FDF4),
    onSurface = Color(0xFFF0FDF4),
    surfaceVariant = Color(0xFF1C3326),
    onSurfaceVariant = Color(0xFF86EFAC),
    outline = Color(0x22FFFFFF)
)

private val LightForestScheme = lightColorScheme(
    primary = Color(0xFF059669),
    secondary = Color(0xFF10B981),
    tertiary = Color(0xFF0891B2),
    background = Color(0xFFF0FDF4),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF064E3B),
    onSurface = Color(0xFF064E3B),
    surfaceVariant = Color(0xFFDCFCE7),
    onSurfaceVariant = Color(0xFF166534),
    outline = Color(0xFFBBF7D0)
)

// 4. Sunset Coral / YouTube Music Velvet
private val DarkSunsetScheme = darkColorScheme(
    primary = Color(0xFFFF5E3A), // Sunset Orange
    secondary = Color(0xFFFF2A6D), // Glowing Coral Pink
    tertiary = Color(0xFFFFB800), // Amber Gold
    background = Color(0xFF140C0B),
    surface = Color(0xFF221614),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color(0xFFFFF5F2),
    onSurface = Color(0xFFFFF5F2),
    surfaceVariant = Color(0xFF2F201D),
    onSurfaceVariant = Color(0xFFFCA5A5),
    outline = Color(0x22FFFFFF)
)

private val LightSunsetScheme = lightColorScheme(
    primary = Color(0xFFEA580C),
    secondary = Color(0xFFE11D48),
    tertiary = Color(0xFFD97706),
    background = Color(0xFFFFF7ED),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF431407),
    onSurface = Color(0xFF431407),
    surfaceVariant = Color(0xFFFFEDD5),
    onSurfaceVariant = Color(0xFF9A3412),
    outline = Color(0xFFFED7AA)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themePreset: AppThemePreset = AppThemePreset.CYBERPUNK,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        when (themePreset) {
            AppThemePreset.CYBERPUNK -> DarkCyberpunkScheme
            AppThemePreset.OCEAN_BLUE -> DarkOceanScheme
            AppThemePreset.FOREST_EMERALD -> DarkForestScheme
            AppThemePreset.SUNSET_GOLD -> DarkSunsetScheme
        }
    } else {
        when (themePreset) {
            AppThemePreset.CYBERPUNK -> LightCyberpunkScheme
            AppThemePreset.OCEAN_BLUE -> LightOceanScheme
            AppThemePreset.FOREST_EMERALD -> LightForestScheme
            AppThemePreset.SUNSET_GOLD -> LightSunsetScheme
        }
    }

    // Keep global static accents in sync with active theme colorScheme
    NeonCyan = colors.primary
    NeonPurple = colors.secondary
    NeonMagenta = colors.tertiary

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}


package com.example.retromultiplataformkotin.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Retro pixel art inspired color palette
object RetroColors {
    // Primary - deep purple/indigo (retro arcade feel)
    val Purple80 = Color(0xFFBB86FC)
    val Purple40 = Color(0xFF6C3FC0)
    val PurpleDark = Color(0xFF1A0A2E)

    // Secondary - neon cyan
    val Cyan80 = Color(0xFF80DEEA)
    val Cyan40 = Color(0xFF00ACC1)

    // Accent - pixel gold
    val Gold = Color(0xFFFFD54F)
    val GoldDark = Color(0xFFFFA000)

    // Background tones
    val DarkBg = Color(0xFF0D1117)
    val DarkSurface = Color(0xFF161B22)
    val DarkCard = Color(0xFF21262D)

    // Phase colors
    val GreenWentWell = Color(0xFF4CAF50)
    val OrangeImprove = Color(0xFFFF9800)
    val BlueAction = Color(0xFF2196F3)
    val PurpleResults = Color(0xFF9C27B0)

    // Text
    val TextPrimary = Color(0xFFE6EDF3)
    val TextSecondary = Color(0xFF8B949E)
    val TextOnAccent = Color(0xFF1A0A2E)
}

private val DarkColorScheme = darkColorScheme(
    primary = RetroColors.Cyan40,
    onPrimary = Color.White,
    primaryContainer = RetroColors.PurpleDark,
    onPrimaryContainer = RetroColors.TextPrimary,
    secondary = RetroColors.Gold,
    onSecondary = RetroColors.TextOnAccent,
    secondaryContainer = RetroColors.DarkCard,
    onSecondaryContainer = RetroColors.TextPrimary,
    background = RetroColors.DarkBg,
    onBackground = RetroColors.TextPrimary,
    surface = RetroColors.DarkSurface,
    onSurface = RetroColors.TextPrimary,
    surfaceVariant = RetroColors.DarkCard,
    onSurfaceVariant = RetroColors.TextSecondary,
    outline = Color(0xFF30363D),
)

@Composable
fun RetroTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = MaterialTheme.typography,
        content = content,
    )
}

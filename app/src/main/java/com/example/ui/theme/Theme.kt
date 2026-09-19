package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = SurfaceWhite,
    primaryContainer = NavyContainer,
    onPrimaryContainer = OnNavyContainer,
    secondary = GoldPrimary,
    onSecondary = SurfaceWhite,
    secondaryContainer = GoldContainer,
    onSecondaryContainer = OnGoldContainer,
    tertiary = GoldAccent,
    onTertiary = NavyDark,
    background = BackgroundLight,
    onBackground = TextDark,
    surface = SurfaceWhite,
    onSurface = TextDark,
    surfaceVariant = BackgroundLight,
    onSurfaceVariant = TextMuted,
    outline = BorderLight
)

private val DarkColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = NavyDark,
    primaryContainer = NavyPrimary,
    onPrimaryContainer = GoldLight,
    secondary = GoldPrimary,
    onSecondary = NavyDark,
    secondaryContainer = NavyLight,
    onSecondaryContainer = SurfaceWhite,
    background = NavyDark,
    onBackground = SurfaceWhite,
    surface = Color(0xFF0F223D),
    onSurface = SurfaceWhite,
    surfaceVariant = Color(0xFF162B4A),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF233B5D)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

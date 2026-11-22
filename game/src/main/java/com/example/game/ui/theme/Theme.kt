package com.example.game.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = LightGreenMedium,
    onPrimary = Color.White,
    secondary = LightOrangeDark,
    onSecondary = Color.White,
    tertiary = LightOrangeLight,
    background = LightGreenLight,
    onBackground = Color(0xFF354438),
    surface = Color.White,
    onSurface = Color(0xFF354438),
    surfaceVariant = LightGreenLight,
    onSurfaceVariant = LightGreenDark,
    outline = LightOrangeDark
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkGreenMedium,
    onPrimary = Color.White,
    secondary = DarkOrangeDark,
    onSecondary = Color.Black,
    tertiary = DarkOrangeLight,
    background = DarkGreenLight,
    onBackground = Color(0xFFE7F0E7),
    surface = DarkSurface,
    onSurface = Color(0xFFE7F0E7),
    surfaceVariant = Color(0xFF2A3C2D),
    onSurfaceVariant = DarkGreenDark,
    outline = DarkOrangeDark
)

@Composable
fun MobileDevLabsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

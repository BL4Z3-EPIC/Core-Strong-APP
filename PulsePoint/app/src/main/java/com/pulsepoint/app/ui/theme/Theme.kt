package com.pulsepoint.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class ThemeMode { SYSTEM, LIGHT, DARK }

private val LightColors = lightColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    primaryContainer = TealContainer,
    onPrimaryContainer = TealDark,
    secondary = Navy,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDE3EA),
    onSecondaryContainer = Color(0xFF15202B),
    tertiary = Amber,
    onTertiary = Color(0xFF4A3200),
    background = BackgroundLight,
    onBackground = Color(0xFF14181A),
    surface = SurfaceWhite,
    onSurface = Color(0xFF14181A),
    surfaceVariant = Color(0xFFE7ECEB),
    onSurfaceVariant = Slate,
    outline = StrokeLight,
    surfaceContainerLow = Color(0xFFF7F9F8),
    surfaceContainer = Color(0xFFF0F3F2),
    surfaceContainerHigh = Color(0xFFE9EDEB),
    surfaceContainerHighest = Color(0xFFE2E7E5),
    error = Color(0xFFBA1A1A)
)

private val DarkColors = darkColorScheme(
    primary = Mint,
    onPrimary = Color(0xFF00372F),
    primaryContainer = Color(0xFF005046),
    onPrimaryContainer = Color(0xFFB2DFDB),
    secondary = Color(0xFFAFC2D6),
    onSecondary = Color(0xFF162C40),
    secondaryContainer = Color(0xFF2E4358),
    onSecondaryContainer = Color(0xFFD5E3F7),
    tertiary = Amber,
    onTertiary = Color(0xFF4A3200),
    background = BackgroundDark,
    onBackground = Color(0xFFE1E4E2),
    surface = SurfaceDark,
    onSurface = Color(0xFFE1E4E2),
    surfaceVariant = Color(0xFF2A3230),
    onSurfaceVariant = Color(0xFFB9C4C0),
    outline = StrokeDark,
    surfaceContainerLow = Color(0xFF141918),
    surfaceContainer = CardDark,
    surfaceContainerHigh = Color(0xFF222A28),
    surfaceContainerHighest = Color(0xFF293331),
    error = Color(0xFFFFB4AB)
)

@Composable
fun PulsePointTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        content = content
    )
}

@Composable
fun pulsePositiveColor(): Color =
    if (MaterialTheme.colorScheme.background == BackgroundDark) PositiveGreenDark else PositiveGreen

@Composable
fun pulseNegativeColor(): Color =
    if (MaterialTheme.colorScheme.background == BackgroundDark) NegativeRedDark else NegativeRed

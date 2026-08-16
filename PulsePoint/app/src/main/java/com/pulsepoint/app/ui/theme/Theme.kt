package com.pulsepoint.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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
    background = Color(0xFF101513),
    onBackground = Color(0xFFE1E3E1),
    surface = Color(0xFF101513),
    onSurface = Color(0xFFE1E3E1),
    surfaceVariant = Color(0xFF3F4946),
    onSurfaceVariant = Color(0xFFBFC9C6),
    error = Color(0xFFFFB4AB)
)

@Composable
fun PulsePointTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        content = content
    )
}

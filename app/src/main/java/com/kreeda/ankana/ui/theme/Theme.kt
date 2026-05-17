package com.kreeda.ankana.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightScheme = lightColorScheme(
    primary = GroundGreen,
    onPrimary = Color.White,
    primaryContainer = GroundGreenLight,
    onPrimaryContainer = DarkPitch,

    secondary = MatchOrange,
    onSecondary = Color.White,
    secondaryContainer = MatchOrangeLight,
    onSecondaryContainer = Color(0xFF3A1500),

    tertiary = Color(0xFF8A6900),
    onTertiary = Color.White,
    tertiaryContainer = ChalkCream,
    onTertiaryContainer = Color(0xFF2A1F00),

    background = SoftBone,
    onBackground = DarkPitch,
    surface = Color.White,
    onSurface = DarkPitch,
    surfaceVariant = ChalkCream,
    onSurfaceVariant = Color(0xFF4F4233),
    outline = Color(0xFFB1A593)
)

private val DarkScheme = darkColorScheme(
    primary = GroundGreenLight,
    onPrimary = DarkPitch,
    primaryContainer = GroundGreenDark,
    onPrimaryContainer = Color.White,

    secondary = MatchOrangeLight,
    onSecondary = Color(0xFF3A1500),
    secondaryContainer = MatchOrangeDark,
    onSecondaryContainer = Color.White,

    tertiary = Color(0xFFFFCC4D),
    onTertiary = Color(0xFF2A1F00),
    tertiaryContainer = Color(0xFF6A5000),
    onTertiaryContainer = ChalkCream,

    background = DarkPitch,
    onBackground = ChalkCream,
    surface = DarkSurface,
    onSurface = ChalkCream,
    surfaceVariant = Color(0xFF233326),
    onSurfaceVariant = Color(0xFFCDC0AC),
    outline = Color(0xFF6E624D)
)

@Composable
fun KreedaAnkanaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val scheme = if (darkTheme) DarkScheme else LightScheme
    MaterialTheme(
        colorScheme = scheme,
        typography = KreedaTypography,
        content = content
    )
}

package com.century.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CenturyRed,
    onPrimary = Color.White,
    primaryContainer = CenturyRedDark,
    onPrimaryContainer = Color.White,
    secondary = TextSecondary,
    onSecondary = Color.White,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    error = CenturyRed,
    tertiary = CenturyGreen,
    onTertiary = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = CenturyRed,
    onPrimary = Color.White,
    primaryContainer = CenturyRedLight,
    onPrimaryContainer = Color.White,
    secondary = LightTextSecondary,
    onSecondary = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    error = CenturyRed,
    tertiary = CenturyGreen,
    onTertiary = Color.White
)

@Composable
fun CenturyTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CenturyTypography,
        content = content
    )
}

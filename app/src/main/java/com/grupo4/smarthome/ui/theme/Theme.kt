package com.grupo4.smarthome.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BrandIndigo,
    onPrimary = Color.White,
    primaryContainer = BrandIndigoLight,
    onPrimaryContainer = BrandIndigoDark,
    secondary = AccentSoft,
    onSecondary = TextMain,
    background = SurfaceAlt,
    onBackground = TextMain,
    surface = Color.White,
    surfaceVariant = SurfaceAlt,
    onSurface = TextMain,
    onSurfaceVariant = TextMuted,
    outline = BorderDefault,
    outlineVariant = StepInactive,
    error = ErrorRed,
    errorContainer = ErrorBg,
    onError = Color.White,
    onErrorContainer = ErrorRed,
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandIndigo,
    onPrimary = Color.White,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = TextMain,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutline,
    error = DarkOnError,
    errorContainer = DarkErrorContainer,
    onError = TextMain,
    onErrorContainer = DarkOnError,
)

@Composable
fun SmartHomeTheme(
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

@Composable
fun SmartHomeTheme(
    themePreference: ThemePreference,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themePreference) {
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
    }
    SmartHomeTheme(darkTheme = darkTheme, content = content)
}

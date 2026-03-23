package com.example.focusable.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightPastelScheme = lightColorScheme(
    primary = PastelLavender,
    onPrimary = PastelDarkText,
    primaryContainer = PastelLavenderLight,
    onPrimaryContainer = PastelDarkText,
    secondary = PastelMint,
    onSecondary = PastelDarkText,
    tertiary = PastelPeach,
    onTertiary = PastelDarkText,
    background = PastelCream,
    onBackground = PastelDarkText,
    surface = PastelSurface,
    onSurface = PastelDarkText,
    surfaceVariant = PastelLavenderLight,
    onSurfaceVariant = PastelMutedText,
    error = PastelSoftError,
    onError = PastelSurface,
    errorContainer = PastelSoftErrorContainer,
    onErrorContainer = PastelDarkText
)

private val DarkFocusScheme = darkColorScheme(
    primary = DeepIndigo,
    onPrimary = LightCream,
    primaryContainer = DeepIndigoMuted,
    onPrimaryContainer = LightCream,
    secondary = DarkTeal,
    onSecondary = LightCream,
    tertiary = MutedRose,
    onTertiary = LightCream,
    background = DeepCharcoal,
    onBackground = LightCream,
    surface = DarkSlate,
    onSurface = LightCream,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = LightCreamMuted,
    error = FocusSoftError,
    onError = DeepCharcoal,
    errorContainer = FocusErrorContainer,
    onErrorContainer = FocusSoftError
)

@Composable
fun FocusableTheme(
    isSessionActive: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isSessionActive) DarkFocusScheme else LightPastelScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

package com.example.audiomemo.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * AudioMemo dark color scheme — Electric Violet (#8B5CF6) theme.
 */
private val AudioMemoDarkColorScheme = darkColorScheme(
    primary                = AccentGreen,
    onPrimary              = TextPrimary,
    primaryContainer       = NavyElevated,
    onPrimaryContainer     = AccentGreenLight,

    secondary              = AccentGreenDark,
    onSecondary            = TextPrimary,
    secondaryContainer     = NavyCard,
    onSecondaryContainer   = TextSecondary,

    tertiary               = LegacyOrange,
    onTertiary             = TextPrimary,
    tertiaryContainer      = NavyCard,
    onTertiaryContainer    = TextSecondary,

    background             = NavyBackground,
    onBackground           = TextPrimary,

    surface                = NavySurface,
    onSurface              = TextPrimary,
    surfaceVariant         = NavyElevated,
    onSurfaceVariant       = TextSecondary,

    outline                = DividerColor,
    outlineVariant         = NavyCard,

    error                  = ErrorRed,
    onError                = TextPrimary,
    errorContainer         = Color(0xFF4B1C1C),
    onErrorContainer       = ErrorRed,
)

/**
 * AudioMemo light color scheme — Sonic Fluid Light.
 * Clean white surfaces with Electric Violet (#8B5CF6) accents.
 */
private val AudioMemoLightColorScheme = lightColorScheme(
    primary                = AccentGreen,
    onPrimary              = Color.White,
    primaryContainer       = LightPrimaryContainer,
    onPrimaryContainer     = LightOnPrimaryContainer,

    secondary              = AccentGreenDark,
    onSecondary            = Color.White,
    secondaryContainer     = Color(0xFFE8DEF8),
    onSecondaryContainer   = Color(0xFF1D192B),

    tertiary               = LegacyOrange,
    onTertiary             = Color.White,
    tertiaryContainer      = Color(0xFFFFD8E4),
    onTertiaryContainer    = Color(0xFF31111D),

    background             = LightBackground,
    onBackground           = LightOnBackground,

    surface                = LightSurface,
    onSurface              = LightOnSurface,
    surfaceVariant         = LightSurfaceVariant,
    onSurfaceVariant       = LightOnSurfaceVariant,

    outline                = LightOutline,
    outlineVariant         = LightOutlineVariant,

    error                  = ErrorRed,
    onError                = Color.White,
    errorContainer         = Color(0xFFFFDAD6),
    onErrorContainer       = Color(0xFF410002),
)

@Composable
fun AudioMemoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AudioMemoDarkColorScheme else AudioMemoLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AudioMemoTypography,
        content     = content
    )
}

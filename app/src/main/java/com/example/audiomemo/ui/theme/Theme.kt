package com.example.audiomemo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * AudioMemo dark color scheme.
 *
 * Slot mapping:
 *   primary            → AccentGreen       (FABs, record button, active tab indicator)
 *   onPrimary          → TextPrimary       (text/icons on green surfaces)
 *   primaryContainer   → NavyElevated      (chip backgrounds, waveform container)
 *   onPrimaryContainer → AccentGreenLight  (icons inside primary containers)
 *
 *   secondary          → AccentGreenDark   (secondary actions, outlined buttons tint)
 *   onSecondary        → TextPrimary
 *   secondaryContainer → NavyCard          (recording status card)
 *   onSecondaryContainer → TextSecondary
 *
 *   tertiary           → LegacyOrange      (accent highlights, "your memories" text)
 *   onTertiary         → TextPrimary
 *
 *   background         → NavyBackground    (all screen backgrounds)
 *   onBackground       → TextPrimary
 *
 *   surface            → NavySurface       (cards, bottom sheets, top bars)
 *   onSurface          → TextPrimary
 *   surfaceVariant     → NavyElevated      (tab rows, divider background)
 *   onSurfaceVariant   → TextSecondary
 *
 *   outline            → DividerColor      (HorizontalDivider, card borders)
 *   outlineVariant     → NavyCard
 *
 *   error              → ErrorRed
 *   onError            → TextPrimary
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

@Composable
fun AudioMemoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AudioMemoDarkColorScheme,
        typography  = AudioMemoTypography,
        content     = content
    )
}

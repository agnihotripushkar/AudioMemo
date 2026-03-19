package com.example.audiomemo.ui.theme

import androidx.compose.ui.graphics.Color

// ── Backgrounds ───────────────────────────────────────────────────────────────
val NavyBackground    = Color(0xFF1A2B3C)   // main screen background
val NavySurface       = Color(0xFF1E3347)   // cards, bottom bars
val NavyElevated      = Color(0xFF243D54)   // elevated cards, dialogs
val NavyCard          = Color(0xFF1B3249)   // subtle card tint

// ── Accent ────────────────────────────────────────────────────────────────────
val AccentGreen       = Color(0xFF4CAF50)   // primary CTA, active states
val AccentGreenLight  = Color(0xFF81C784)   // on-primary-container, icons on dark
val AccentGreenDark   = Color(0xFF388E3C)   // pressed / secondary actions

// ── Text ──────────────────────────────────────────────────────────────────────
val TextPrimary       = Color(0xFFFFFFFF)   // headings, body on dark bg
val TextSecondary     = Color(0xFFB0BEC5)   // subtitles, captions
val TextTertiary      = Color(0xFF78909C)   // placeholders, hints
val TextDisabled      = Color(0xFF546E7A)   // disabled labels

// ── Semantic ──────────────────────────────────────────────────────────────────
val ErrorRed          = Color(0xFFEF5350)
val RecordingRed      = Color(0xFFFF5252)   // live recording dot / stop square
val SuccessGreen      = Color(0xFF66BB6A)   // transcription complete badge
val DividerColor      = Color(0xFF2A3F54)   // HorizontalDivider, tab underline

// ── Hero gradient stops (used in HomeScreen canvas) ───────────────────────────
val HeroSkyTop        = Color(0xFF1A2B3C)
val HeroSkyMid        = Color(0xFF1E3A50)
val HeroSkyBottom     = Color(0xFF243D54)

// ── Legacy (kept so existing un-redesigned screens still compile) ─────────────
val LegacyDarkTeal    = Color(0xFF1A3D4E)
val LegacyOrange      = Color(0xFFE8872A)
val LegacyLightBlue   = Color(0xFFD4E8F4)
val LegacyPeach       = Color(0xFFF5E0CA)

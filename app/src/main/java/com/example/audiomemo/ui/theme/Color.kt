package com.example.audiomemo.ui.theme

import androidx.compose.ui.graphics.Color

// ── Backgrounds ───────────────────────────────────────────────────────────────
val NavyBackground    = Color(0xFF0D0B18)   // main screen background  (deep indigo-black)
val NavySurface       = Color(0xFF1A1730)   // cards, bottom bars       (dark indigo)
val NavyElevated      = Color(0xFF231F3A)   // elevated cards, dialogs  (lifted indigo)
val NavyCard          = Color(0xFF1E1935)   // subtle card tint

// ── Accent — Electric Violet ───────────────────────────────────────────────────
val AccentGreen       = Color(0xFF8B5CF6)   // primary CTA, active states  (#8B5CF6 Electric Violet)
val AccentGreenLight  = Color(0xFFA78BFA)   // on-primary-container, icons on dark (violet-400)
val AccentGreenDark   = Color(0xFF7C3AED)   // pressed / secondary actions  (violet-600)

// ── Text ──────────────────────────────────────────────────────────────────────
val TextPrimary       = Color(0xFFFFFFFF)   // headings, body on dark bg
val TextSecondary     = Color(0xFFB0B0D8)   // subtitles, captions
val TextTertiary      = Color(0xFF7878A8)   // placeholders, hints
val TextDisabled      = Color(0xFF4E4E7A)   // disabled labels

// ── Semantic ──────────────────────────────────────────────────────────────────
val ErrorRed          = Color(0xFFEF5350)
val RecordingRed      = Color(0xFFFF5252)   // live recording dot / stop square
val SuccessGreen      = Color(0xFF66BB6A)   // transcription complete badge
val DividerColor      = Color(0xFF2A2545)   // HorizontalDivider, tab underline

// ── Hero gradient stops (used in HomeScreen canvas) ───────────────────────────
val HeroSkyTop        = Color(0xFF0D0B18)
val HeroSkyMid        = Color(0xFF160E2E)
val HeroSkyBottom     = Color(0xFF1E1738)

// ── Legacy (kept so existing un-redesigned screens still compile) ─────────────
val LegacyDarkTeal    = Color(0xFF1A1235)   // was dark teal, now deep violet
val LegacyOrange      = Color(0xFFF472B6)   // tertiary pink accent (from Electric Violet palette)
val LegacyLightBlue   = Color(0xFFDDD6FE)   // was light blue, now light violet (violet-200)
val LegacyPeach       = Color(0xFFF5F3FF)   // was peach, now lavender tint (violet-50)

// ── Light Theme Palette (Sonic Fluid Light) ───────────────────────────────────
val LightBackground        = Color(0xFFFFFBFE)   // airy white — M3 standard light bg
val LightSurface           = Color(0xFFFFFFFF)   // pure white cards
val LightSurfaceVariant    = Color(0xFFEDE8F5)   // light violet-tinted container
val LightCard              = Color(0xFFF3EEF8)   // subtle violet-wash card tint
val LightOnBackground      = Color(0xFF1C1035)   // deep violet-black primary text
val LightOnSurface         = Color(0xFF1C1035)   // text on cards
val LightOnSurfaceVariant  = Color(0xFF49454F)   // secondary text (gray-violet)
val LightOutline           = Color(0xFF79747E)   // borders + tertiary text
val LightOutlineVariant    = Color(0xFFCAC4D0)   // subtle dividers
val LightPrimaryContainer  = Color(0xFFEADDFF)   // light violet chip / container bg
val LightOnPrimaryContainer= Color(0xFF21005D)   // dark violet text on light container

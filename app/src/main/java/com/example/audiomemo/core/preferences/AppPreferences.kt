package com.example.audiomemo.core.preferences

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.example.audiomemo.R
import com.example.audiomemo.ui.theme.DancingScriptFontFamily
import com.example.audiomemo.ui.theme.NunitoFontFamily
import com.example.audiomemo.ui.theme.PlayfairDisplayFontFamily
import com.example.audiomemo.ui.theme.PlusJakartaSansFontFamily
import com.example.audiomemo.ui.theme.PoppinsFontFamily
import com.example.audiomemo.ui.theme.RobotoMonoFontFamily
import com.example.audiomemo.ui.theme.SpaceGroteskFontFamily

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class AccentColor(
    val displayName: String,
    val color: Color,
    val dark: Color,
    val light: Color,
) {
    VIOLET("Violet",  Color(0xFF8B5CF6), Color(0xFF7C3AED), Color(0xFFA78BFA)),
    INDIGO("Indigo",  Color(0xFF6366F1), Color(0xFF4F46E5), Color(0xFF818CF8)),
    BLUE("Blue",      Color(0xFF3B82F6), Color(0xFF2563EB), Color(0xFF60A5FA)),
    CYAN("Cyan",      Color(0xFF06B6D4), Color(0xFF0891B2), Color(0xFF22D3EE)),
    EMERALD("Emerald",Color(0xFF10B981), Color(0xFF059669), Color(0xFF34D399)),
    ORANGE("Orange",  Color(0xFFF97316), Color(0xFFEA580C), Color(0xFFFB923C)),
    ROSE("Rose",      Color(0xFFF43F5E), Color(0xFFE11D48), Color(0xFFFB7185)),
    PINK("Pink",      Color(0xFFEC4899), Color(0xFFDB2777), Color(0xFFF472B6)),
}

enum class AppFont(
    val displayName: String,
    @StringRes val descriptionRes: Int
) {
    DEFAULT("Default", R.string.font_desc_default),
    NUNITO("Nunito", R.string.font_desc_nunito),
    POPPINS("Poppins", R.string.font_desc_poppins),
    PLUS_JAKARTA_SANS("Plus Jakarta Sans", R.string.font_desc_plus_jakarta),
    SPACE_GROTESK("Space Grotesk", R.string.font_desc_space_grotesk),
    PLAYFAIR_DISPLAY("Playfair Display", R.string.font_desc_playfair),
    DANCING_SCRIPT("Dancing Script", R.string.font_desc_dancing_script),
    ROBOTO_MONO("Roboto Mono", R.string.font_desc_roboto_mono);

    fun toFontFamily(): FontFamily = when (this) {
        DEFAULT -> FontFamily.Default
        NUNITO -> NunitoFontFamily
        POPPINS -> PoppinsFontFamily
        PLUS_JAKARTA_SANS -> PlusJakartaSansFontFamily
        SPACE_GROTESK -> SpaceGroteskFontFamily
        PLAYFAIR_DISPLAY -> PlayfairDisplayFontFamily
        DANCING_SCRIPT -> DancingScriptFontFamily
        ROBOTO_MONO -> RobotoMonoFontFamily
    }
}

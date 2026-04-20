package com.example.audiomemo.core.preferences

import androidx.annotation.StringRes
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

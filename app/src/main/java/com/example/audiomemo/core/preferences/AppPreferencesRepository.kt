package com.example.audiomemo.core.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val THEME_MODE   = stringPreferencesKey("theme_mode")
        val APP_FONT     = stringPreferencesKey("app_font")
        val ACCENT_COLOR = stringPreferencesKey("accent_color")
    }

    val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        ThemeMode.entries.firstOrNull { it.name == prefs[Keys.THEME_MODE] } ?: ThemeMode.SYSTEM
    }

    val appFont: Flow<AppFont> = dataStore.data.map { prefs ->
        AppFont.entries.firstOrNull { it.name == prefs[Keys.APP_FONT] } ?: AppFont.DEFAULT
    }

    val accentColor: Flow<AccentColor> = dataStore.data.map { prefs ->
        AccentColor.entries.firstOrNull { it.name == prefs[Keys.ACCENT_COLOR] } ?: AccentColor.VIOLET
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setAppFont(font: AppFont) {
        dataStore.edit { it[Keys.APP_FONT] = font.name }
    }

    suspend fun setAccentColor(color: AccentColor) {
        dataStore.edit { it[Keys.ACCENT_COLOR] = color.name }
    }
}

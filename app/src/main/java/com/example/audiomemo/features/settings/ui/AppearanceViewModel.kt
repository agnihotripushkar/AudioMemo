package com.example.audiomemo.features.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audiomemo.core.preferences.AccentColor
import com.example.audiomemo.core.preferences.AppFont
import com.example.audiomemo.core.preferences.AppPreferencesRepository
import com.example.audiomemo.core.preferences.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppearanceViewModel @Inject constructor(
    private val repo: AppPreferencesRepository
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = repo.themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ThemeMode.SYSTEM
    )

    val appFont: StateFlow<AppFont> = repo.appFont.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppFont.DEFAULT
    )

    val accentColor: StateFlow<AccentColor> = repo.accentColor.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AccentColor.VIOLET
    )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repo.setThemeMode(mode) }
    }

    fun setAppFont(font: AppFont) {
        viewModelScope.launch { repo.setAppFont(font) }
    }

    fun setAccentColor(color: AccentColor) {
        viewModelScope.launch { repo.setAccentColor(color) }
    }
}

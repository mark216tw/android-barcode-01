package com.simplebarcode.app.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class DarkMode(val displayName: String) {
    SYSTEM("跟隨系統"),
    LIGHT("淺色"),
    DARK("深色"),
}

data class AppSettings(
    val themeIndex: Int = 0,
    val customHue: Float = 46f,
    val useCustomTheme: Boolean = false,
    val darkMode: DarkMode = DarkMode.SYSTEM,
)

class SettingsRepository(context: Context) {
    private val preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    private val _settings = MutableStateFlow(load())

    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    fun setTheme(index: Int, hue: Float) {
        val updated = _settings.value.copy(
            themeIndex = index.coerceIn(0, 5),
            customHue = hue.coerceIn(0f, 360f),
            useCustomTheme = false,
        )
        preferences.edit {
            putInt(KEY_THEME, updated.themeIndex)
            putFloat(KEY_CUSTOM_HUE, updated.customHue)
            putBoolean(KEY_USE_CUSTOM_THEME, false)
        }
        _settings.value = updated
    }

    fun setCustomHue(hue: Float) {
        val updated = _settings.value.copy(
            customHue = hue.coerceIn(0f, 360f),
            useCustomTheme = true,
        )
        preferences.edit {
            putFloat(KEY_CUSTOM_HUE, updated.customHue)
            putBoolean(KEY_USE_CUSTOM_THEME, true)
        }
        _settings.value = updated
    }

    fun useCustomTheme() {
        if (_settings.value.useCustomTheme) return
        preferences.edit { putBoolean(KEY_USE_CUSTOM_THEME, true) }
        _settings.value = _settings.value.copy(useCustomTheme = true)
    }

    fun setDarkMode(mode: DarkMode) {
        val updated = _settings.value.copy(darkMode = mode)
        preferences.edit { putString(KEY_DARK_MODE, mode.name) }
        _settings.value = updated
    }

    private fun load(): AppSettings = AppSettings(
        themeIndex = preferences.getInt(KEY_THEME, 0).coerceIn(0, 5),
        customHue = preferences.getFloat(KEY_CUSTOM_HUE, 46f).coerceIn(0f, 360f),
        useCustomTheme = preferences.getBoolean(KEY_USE_CUSTOM_THEME, false),
        darkMode = runCatching {
            DarkMode.valueOf(preferences.getString(KEY_DARK_MODE, DarkMode.SYSTEM.name).orEmpty())
        }.getOrDefault(DarkMode.SYSTEM),
    )

    companion object {
        const val FILE_NAME = "settings"
        const val KEY_THEME = "theme_index"
        private const val KEY_CUSTOM_HUE = "custom_hue"
        private const val KEY_USE_CUSTOM_THEME = "use_custom_theme"
        private const val KEY_DARK_MODE = "dark_mode"
    }
}

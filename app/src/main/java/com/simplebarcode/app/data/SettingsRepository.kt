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
    val darkMode: DarkMode = DarkMode.SYSTEM,
)

class SettingsRepository(context: Context) {
    private val preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    private val _settings = MutableStateFlow(load())

    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    fun setTheme(index: Int) {
        val updated = _settings.value.copy(themeIndex = index.coerceIn(0, 5))
        preferences.edit { putInt(KEY_THEME, updated.themeIndex) }
        _settings.value = updated
    }

    fun setDarkMode(mode: DarkMode) {
        val updated = _settings.value.copy(darkMode = mode)
        preferences.edit { putString(KEY_DARK_MODE, mode.name) }
        _settings.value = updated
    }

    private fun load(): AppSettings = AppSettings(
        themeIndex = preferences.getInt(KEY_THEME, 0).coerceIn(0, 5),
        darkMode = runCatching {
            DarkMode.valueOf(preferences.getString(KEY_DARK_MODE, DarkMode.SYSTEM.name).orEmpty())
        }.getOrDefault(DarkMode.SYSTEM),
    )

    companion object {
        const val FILE_NAME = "settings"
        const val KEY_THEME = "theme_index"
        private const val KEY_DARK_MODE = "dark_mode"
    }
}

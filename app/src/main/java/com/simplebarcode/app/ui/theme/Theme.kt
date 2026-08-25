package com.simplebarcode.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.simplebarcode.app.data.DarkMode

data class ThemeOption(
    val name: String,
    val primary: Color,
    val darkPrimary: Color,
    val container: Color,
)

val ThemeOptions = listOf(
    ThemeOption("晴空藍", Color(0xFF3478F6), Color(0xFFADC6FF), Color(0xFFD9E5FF)),
    ThemeOption("薄荷綠", Color(0xFF168873), Color(0xFF75DBC2), Color(0xFFB6F2E2)),
    ThemeOption("柳橙橘", Color(0xFFE76620), Color(0xFFFFB68E), Color(0xFFFFDBCA)),
    ThemeOption("珊瑚紅", Color(0xFFDD4053), Color(0xFFFFB2B9), Color(0xFFFFDADC)),
    ThemeOption("葡萄紫", Color(0xFF7652B5), Color(0xFFD0BCFF), Color(0xFFE9DDFF)),
    ThemeOption("蜂蜜黃", Color(0xFF8B6D00), Color(0xFFEBCB58), Color(0xFFFFEFAE)),
)

@Composable
fun SimpleBarcodeTheme(
    themeIndex: Int,
    darkMode: DarkMode,
    content: @Composable (isDark: Boolean) -> Unit,
) {
    val isDark = when (darkMode) {
        DarkMode.SYSTEM -> isSystemInDarkTheme()
        DarkMode.LIGHT -> false
        DarkMode.DARK -> true
    }
    val option = ThemeOptions[themeIndex.coerceIn(ThemeOptions.indices)]
    val colors = if (isDark) {
        darkColorScheme(
            primary = option.darkPrimary,
            onPrimary = Color(0xFF10213D),
            primaryContainer = option.primary.copy(alpha = 0.48f),
            secondaryContainer = Color(0xFF34353A),
            background = Color(0xFF111318),
            surface = Color(0xFF111318),
            surfaceContainer = Color(0xFF1C1E24),
        )
    } else {
        lightColorScheme(
            primary = option.primary,
            onPrimary = Color.White,
            primaryContainer = option.container,
            onPrimaryContainer = Color(0xFF17213A),
            secondaryContainer = option.container.copy(alpha = 0.7f),
            background = Color(0xFFF8F9FF),
            surface = Color(0xFFF8F9FF),
            surfaceContainer = Color.White,
        )
    }

    MaterialTheme(colorScheme = colors) { content(isDark) }
}

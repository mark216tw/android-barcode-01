package com.simplebarcode.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.simplebarcode.app.data.DarkMode

data class ThemeOption(
    val name: String,
    val hue: Float,
    val primary: Color,
    val darkPrimary: Color,
    val container: Color,
)

val ThemeOptions = listOf(
    ThemeOption("晴空藍", 216f, Color(0xFF3478F6), Color(0xFFADC6FF), Color(0xFFD9E5FF)),
    ThemeOption("薄荷綠", 168f, Color(0xFF168873), Color(0xFF75DBC2), Color(0xFFB6F2E2)),
    ThemeOption("柳橙橘", 20f, Color(0xFFE76620), Color(0xFFFFB68E), Color(0xFFFFDBCA)),
    ThemeOption("珊瑚紅", 353f, Color(0xFFDD4053), Color(0xFFFFB2B9), Color(0xFFFFDADC)),
    ThemeOption("葡萄紫", 264f, Color(0xFF7652B5), Color(0xFFD0BCFF), Color(0xFFE9DDFF)),
    ThemeOption("蜂蜜黃", 47f, Color(0xFF8B6D00), Color(0xFFEBCB58), Color(0xFFFFEFAE)),
)

fun customThemeOption(hue: Float): ThemeOption {
    val safeHue = hue.coerceIn(0f, 360f)
    return ThemeOption(
        name = "自訂色彩",
        hue = safeHue,
        primary = Color.hsv(safeHue, 0.78f, 0.72f),
        darkPrimary = Color.hsv(safeHue, 0.48f, 0.96f),
        container = Color.hsv(safeHue, 0.22f, 1f),
    )
}

@Composable
fun SimpleBarcodeTheme(
    themeIndex: Int,
    customHue: Float,
    useCustomTheme: Boolean,
    darkMode: DarkMode,
    content: @Composable (isDark: Boolean) -> Unit,
) {
    val isDark = when (darkMode) {
        DarkMode.SYSTEM -> isSystemInDarkTheme()
        DarkMode.LIGHT -> false
        DarkMode.DARK -> true
    }
    val option = if (useCustomTheme) customThemeOption(customHue) else ThemeOptions[themeIndex.coerceIn(ThemeOptions.indices)]
    val colors = if (isDark) {
        darkColorScheme(
            primary = option.darkPrimary,
            onPrimary = contentColorFor(option.darkPrimary),
            primaryContainer = option.primary.copy(alpha = 0.48f),
            secondaryContainer = Color(0xFF34353A),
            background = Color(0xFF111318),
            surface = Color(0xFF111318),
            surfaceContainer = Color(0xFF1C1E24),
        )
    } else {
        lightColorScheme(
            primary = option.primary,
            onPrimary = contentColorFor(option.primary),
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

private fun contentColorFor(background: Color): Color =
    if (background.luminance() > 0.48f) Color(0xFF17191F) else Color.White

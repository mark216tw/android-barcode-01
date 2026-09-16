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
    themeOption("暖陽黃", 46f, Color(0xFF765B00)),
    themeOption("珊瑚紅", 352f, Color(0xFFA63C4A)),
    themeOption("活力橘", 28f, Color(0xFF974700)),
    themeOption("青草綠", 101f, Color(0xFF386A20)),
    themeOption("天空藍", 196f, Color(0xFF00658B)),
    themeOption("葡萄紫", 248f, Color(0xFF6555C7)),
)

private fun themeOption(name: String, hue: Float, primary: Color) = ThemeOption(
    name = name,
    hue = hue,
    primary = primary,
    darkPrimary = Color.hsv(hue, 0.48f, 0.96f),
    container = Color.hsv(hue, 0.22f, 1f),
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

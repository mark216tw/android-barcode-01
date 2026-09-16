package com.simplebarcode.app

import androidx.compose.ui.graphics.toArgb
import com.simplebarcode.app.data.AppSettings
import com.simplebarcode.app.ui.theme.ThemeOptions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildAndThemeTest {
    @Test
    fun buildIdUsesTimestampFormat() {
        assertTrue(BuildConfig.BUILD_ID.matches(Regex("\\d{8}\\.\\d{6}\\.\\d{3}")))
    }

    @Test
    fun themeOptionsUseConfiguredNamesAndColors() {
        assertEquals(
            listOf("暖陽黃", "珊瑚紅", "活力橘", "青草綠", "天空藍", "葡萄紫"),
            ThemeOptions.map { it.name },
        )
        assertEquals(
            listOf(0xFF765B00, 0xFFA63C4A, 0xFF974700, 0xFF386A20, 0xFF00658B, 0xFF6555C7),
            ThemeOptions.map { it.primary.toArgb().toLong() and 0xFFFFFFFF },
        )
    }

    @Test
    fun warmYellowIsTheDefaultTheme() {
        val settings = AppSettings()

        assertEquals(0, settings.themeIndex)
        assertEquals(46f, settings.customHue)
        assertEquals("暖陽黃", ThemeOptions[settings.themeIndex].name)
    }
}

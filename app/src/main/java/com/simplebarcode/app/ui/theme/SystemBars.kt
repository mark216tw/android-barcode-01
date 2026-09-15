package com.simplebarcode.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Suppress("DEPRECATION")
@Composable
fun SyncSystemBars() {
    val view = LocalView.current
    val surface = MaterialTheme.colorScheme.surface
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val useDarkIcons = surface.luminance() > 0.5f
            window.statusBarColor = surface.toArgb()
            window.navigationBarColor = surface.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = useDarkIcons
                isAppearanceLightNavigationBars = useDarkIcons
            }
        }
    }
}

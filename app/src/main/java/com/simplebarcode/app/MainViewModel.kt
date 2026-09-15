package com.simplebarcode.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.simplebarcode.app.data.AppSettings
import com.simplebarcode.app.data.BarcodeItem
import com.simplebarcode.app.data.BarcodeRepository
import com.simplebarcode.app.data.BarcodeType
import com.simplebarcode.app.data.DarkMode
import com.simplebarcode.app.data.SettingsRepository
import com.simplebarcode.app.widget.BarcodeWidgetProvider
import kotlinx.coroutines.flow.StateFlow

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val barcodeRepository = BarcodeRepository(application)
    private val settingsRepository = SettingsRepository(application)

    val items: StateFlow<List<BarcodeItem>> = barcodeRepository.items
    val settings: StateFlow<AppSettings> = settingsRepository.settings

    fun getItem(id: Long): BarcodeItem? = barcodeRepository.getById(id)

    fun save(
        id: Long?,
        name: String,
        content: String,
        type: BarcodeType,
        showText: Boolean,
    ): Long {
        val existing = id?.let(barcodeRepository::getById)
        val savedId = existing?.id ?: System.currentTimeMillis()
        barcodeRepository.save(
            BarcodeItem(
                id = savedId,
                name = name.trim(),
                content = content.trim(),
                type = type,
                showText = showText,
                isFavorite = existing?.isFavorite ?: false,
                createdAt = existing?.createdAt ?: System.currentTimeMillis(),
            ),
        )
        refreshWidgets()
        return savedId
    }

    fun delete(id: Long) {
        barcodeRepository.delete(id)
        refreshWidgets()
    }

    fun toggleFavorite(id: Long) {
        barcodeRepository.toggleFavorite(id)
    }

    fun setTheme(index: Int, hue: Float) {
        settingsRepository.setTheme(index, hue)
    }

    fun setCustomHue(hue: Float) {
        settingsRepository.setCustomHue(hue)
    }

    fun useCustomTheme() {
        settingsRepository.useCustomTheme()
    }

    fun setDarkMode(mode: DarkMode) {
        settingsRepository.setDarkMode(mode)
    }

    private fun refreshWidgets() {
        BarcodeWidgetProvider.updateAll(getApplication())
    }
}

package com.simplebarcode.app.widget

import android.content.Context
import androidx.core.content.edit

object WidgetStore {
    private const val FILE_NAME = "widget_selections"

    fun save(context: Context, appWidgetId: Int, barcodeId: Long) {
        preferences(context).edit { putLong(appWidgetId.toString(), barcodeId) }
    }

    fun get(context: Context, appWidgetId: Int): Long? {
        val value = preferences(context).getLong(appWidgetId.toString(), -1L)
        return value.takeIf { it > 0 }
    }

    fun delete(context: Context, appWidgetId: Int) {
        preferences(context).edit { remove(appWidgetId.toString()) }
    }

    private fun preferences(context: Context) =
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
}

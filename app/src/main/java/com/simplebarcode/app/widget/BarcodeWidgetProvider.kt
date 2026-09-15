package com.simplebarcode.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.simplebarcode.app.MainActivity
import com.simplebarcode.app.R
import com.simplebarcode.app.barcode.BarcodeGenerator
import com.simplebarcode.app.data.BarcodeRepository
import kotlin.math.roundToInt

class BarcodeWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { update(context, manager, it) }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        manager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle,
    ) {
        update(context, manager, appWidgetId)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        appWidgetIds.forEach { WidgetStore.delete(context, it) }
    }

    companion object {
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, BarcodeWidgetProvider::class.java)
            manager.getAppWidgetIds(component).forEach { update(context, manager, it) }
        }

        fun update(context: Context, manager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_barcode)
            views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.widget_background)

            val barcodeId = WidgetStore.get(context, appWidgetId)
            val item = barcodeId?.let { BarcodeRepository(context).getById(it) }
            if (item == null) {
                views.setTextViewText(R.id.widget_title, context.getString(R.string.widget_empty))
                views.setViewVisibility(R.id.widget_title, View.VISIBLE)
                views.setViewVisibility(R.id.widget_barcode, View.GONE)
                manager.updateAppWidget(appWidgetId, views)
                return
            }

            val options = manager.getAppWidgetOptions(appWidgetId)
            val widthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 180).coerceAtLeast(110)
            val heightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 70).coerceAtLeast(48)
            val density = context.resources.displayMetrics.density
            val imageHeightDp = heightDp - 20
            val widthPx = (widthDp * density).roundToInt().coerceIn(320, 1600)
            val heightPx = (imageHeightDp * density).roundToInt().coerceIn(100, 600)

            val bitmap = runCatching {
                BarcodeGenerator.createBitmap(item.content, item.type, widthPx, heightPx, margin = 12)
            }.getOrNull()
            views.setTextViewText(R.id.widget_title, item.name)
            views.setViewVisibility(R.id.widget_title, View.VISIBLE)
            views.setViewVisibility(R.id.widget_barcode, if (bitmap != null) View.VISIBLE else View.GONE)
            bitmap?.let { views.setImageViewBitmap(R.id.widget_barcode, it) }

            val openIntent = Intent(context, MainActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_BARCODE_ID, item.id)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            manager.updateAppWidget(appWidgetId, views)
        }
    }
}

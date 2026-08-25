package com.simplebarcode.app.barcode

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.simplebarcode.app.data.BarcodeItem
import java.io.File
import java.io.FileOutputStream

fun shareBarcode(context: Context, item: BarcodeItem) {
    runCatching {
        val directory = File(context.cacheDir, "shared").apply { mkdirs() }
        val safeId = item.id.toString()
        val file = File(directory, "barcode_$safeId.png")
        val bitmap = BarcodeGenerator.createBitmap(item.content, item.type, 1600, 600, margin = 24)
        FileOutputStream(file).use { output ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, output)
        }
        bitmap.recycle()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "${item.name}\n${item.content}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "分享條碼"))
    }
}

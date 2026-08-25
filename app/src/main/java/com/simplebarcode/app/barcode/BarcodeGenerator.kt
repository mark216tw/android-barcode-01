package com.simplebarcode.app.barcode

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.simplebarcode.app.data.BarcodeType

object BarcodeGenerator {
    fun validate(content: String, type: BarcodeType): String? {
        if (content.isBlank()) return "請輸入條碼內容"

        val formatError = when (type) {
            BarcodeType.EAN_13 -> digitLengthError(content, setOf(12, 13), "EAN-13 需要 12 或 13 位數字")
            BarcodeType.EAN_8 -> digitLengthError(content, setOf(7, 8), "EAN-8 需要 7 或 8 位數字")
            BarcodeType.UPC_A -> digitLengthError(content, setOf(11, 12), "UPC-A 需要 11 或 12 位數字")
            BarcodeType.ITF -> if (!content.all(Char::isDigit) || content.length % 2 != 0) "ITF 需要偶數位數字" else null
            else -> null
        }
        if (formatError != null) return formatError

        return runCatching { encode(content, type, 320, 120) }
            .exceptionOrNull()
            ?.let { "內容不符合 ${type.displayName} 格式" }
    }

    fun createBitmap(
        content: String,
        type: BarcodeType,
        width: Int,
        height: Int,
        margin: Int = 12,
    ): Bitmap {
        val matrix = encode(content, type, width, height, margin)
        val pixels = IntArray(matrix.width * matrix.height)
        for (y in 0 until matrix.height) {
            val offset = y * matrix.width
            for (x in 0 until matrix.width) {
                pixels[offset + x] = if (matrix[x, y]) Color.BLACK else Color.WHITE
            }
        }
        return Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, matrix.width, 0, 0, matrix.width, matrix.height)
        }
    }

    private fun encode(
        content: String,
        type: BarcodeType,
        width: Int,
        height: Int,
        margin: Int = 12,
    ) = MultiFormatWriter().encode(
        content,
        type.format,
        width.coerceAtLeast(1),
        height.coerceAtLeast(1),
        mapOf(
            EncodeHintType.MARGIN to margin,
            EncodeHintType.CHARACTER_SET to "UTF-8",
        ),
    )

    private fun digitLengthError(content: String, lengths: Set<Int>, message: String): String? =
        if (!content.all(Char::isDigit) || content.length !in lengths) message else null
}

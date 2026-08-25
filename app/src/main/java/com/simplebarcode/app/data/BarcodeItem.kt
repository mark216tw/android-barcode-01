package com.simplebarcode.app.data

import com.google.zxing.BarcodeFormat

enum class BarcodeType(
    val displayName: String,
    val format: BarcodeFormat,
    val inputHint: String,
) {
    CODE_128("Code 128", BarcodeFormat.CODE_128, "可輸入英文字母、數字與符號"),
    CODE_39("Code 39", BarcodeFormat.CODE_39, "英文字母、數字及 - . 空格 \$ / + %"),
    EAN_13("EAN-13", BarcodeFormat.EAN_13, "輸入 12 或 13 位數字"),
    EAN_8("EAN-8", BarcodeFormat.EAN_8, "輸入 7 或 8 位數字"),
    UPC_A("UPC-A", BarcodeFormat.UPC_A, "輸入 11 或 12 位數字"),
    ITF("ITF", BarcodeFormat.ITF, "輸入偶數位數字"),
    CODABAR("Codabar", BarcodeFormat.CODABAR, "例如 A123456A"),
}

data class BarcodeItem(
    val id: Long,
    val name: String,
    val content: String,
    val type: BarcodeType,
    val showText: Boolean = true,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

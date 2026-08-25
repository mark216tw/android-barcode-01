package com.simplebarcode.app.barcode

import com.simplebarcode.app.data.BarcodeType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BarcodeGeneratorTest {
    @Test
    fun validCode128HasNoError() {
        assertNull(BarcodeGenerator.validate("MEMBER-12345", BarcodeType.CODE_128))
    }

    @Test
    fun ean13RejectsWrongLength() {
        assertEquals(
            "EAN-13 需要 12 或 13 位數字",
            BarcodeGenerator.validate("12345", BarcodeType.EAN_13),
        )
    }

    @Test
    fun itfRequiresEvenNumberOfDigits() {
        assertEquals(
            "ITF 需要偶數位數字",
            BarcodeGenerator.validate("123", BarcodeType.ITF),
        )
    }
}

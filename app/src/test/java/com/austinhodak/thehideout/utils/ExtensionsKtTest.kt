package com.austinhodak.thehideout.utils

import com.austinhodak.thehideout.compose.theme.Green500
import com.austinhodak.thehideout.compose.theme.Red500
import junit.framework.TestCase

class ExtensionsKtTest : TestCase() {

    fun testRoubleToDollar() {
        assertEquals((121000).roubleToDollar(), 1000)
    }

    fun testAsColor() {
        val color = 1.0.asColor()
        val color2 = (-1.0).asColor()

        assertEquals(color, Green500)
        assertEquals(color2, Red500)
    }

    fun testAsBlocks() {
        assertEquals(true.asBlocks(), "YES")
        assertEquals(false.asBlocks(), "NO")
    }

    fun testAddQuotes() {
        val quotes = (10).addQuotes()
        assertEquals(quotes, "\"10\"")
    }
}
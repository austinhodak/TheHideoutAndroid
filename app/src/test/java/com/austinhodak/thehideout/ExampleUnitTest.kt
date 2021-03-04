package com.austinhodak.thehideout

import com.austinhodak.thehideout.calculator.models.Body
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun test() {
        val body = Body()
        body.reset()
        assertEquals(35, body.head.health)
    }
}
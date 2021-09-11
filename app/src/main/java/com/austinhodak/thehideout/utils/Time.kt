package com.austinhodak.thehideout.utils

import java.text.SimpleDateFormat
import java.util.*

object Time {

    private val tarkovRatio = 7
    val formatter = SimpleDateFormat("HH:mm:ss").apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun realTimeToTarkovTime(left: Boolean): String {

        val oneDay = hrs(24)
        val russia = hrs(3)

        val offset = if (left) {
            russia
        } else {
            russia + hrs(12)
        }

        return Date((offset + (System.currentTimeMillis() * tarkovRatio)) % oneDay).formatTime()
    }

    private fun hrs(num: Long): Long {
        return 1000 * 60 * 60 * num
    }

    private fun Date.formatTime(): String = formatter.format(this)
}
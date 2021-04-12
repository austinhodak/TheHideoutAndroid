package com.austinhodak.thehideout.utils

import java.util.*

object Time {

    private val tarkovRatio = 7

    fun realTimeToTarkovTime(left: Boolean): Date {

        val oneDay = hrs(24)
        val russia = hrs(3)

        val offset = if (left) {
            russia
        } else {
            russia + hrs(12)
        }

        return Date((offset + (System.currentTimeMillis() * tarkovRatio)) % oneDay)
    }

    fun hrs(num: Long): Long {
        return 1000 * 60 * 60 * num
    }
}
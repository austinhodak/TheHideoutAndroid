package com.austinhodak.thehideout.viewmodels.models

import com.austinhodak.thehideout.getCurrency
import com.austinhodak.thehideout.getTraderLevel
import java.text.DecimalFormat

data class Price (
    var value: Int,
    var _id: String,
    var trader: String,
    var currency: String,
    var level: Int
) {
    override fun toString(): String {
        val format = DecimalFormat("###.##")
        return "${level.getTraderLevel()} $trader ${currency.getCurrency()}${format.format(value)}"
    }
}
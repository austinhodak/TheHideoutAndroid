package com.austinhodak.thehideout.clothing.models

import com.austinhodak.tarkovapi.utils.getTraderLevel
import com.austinhodak.thehideout.utils.getCurrency
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
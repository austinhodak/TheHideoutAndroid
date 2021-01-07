package com.austinhodak.thehideout.viewmodels.models

import com.austinhodak.thehideout.getTraderLevel
import java.text.DecimalFormat

data class Tradeup(
    var _id: String,
    var trader: String,
    var level: Int
) {
    override fun toString(): String {
        val format = DecimalFormat("###.##")
        return "${level.getTraderLevel()} $trader Tradeup"
    }
}
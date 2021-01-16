package com.austinhodak.thehideout.viewmodels.models

import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.getCurrency
import com.austinhodak.thehideout.getTraderLevel
import kotlinx.serialization.Serializable
import java.text.DecimalFormat

@Serializable
data class AmmoModel (
    var description: String,
    var weight: Double,
    var velocity: Int,
    var damage: Int,
    var penetration: Int,
    var recoil: Double,
    var accuracy: Double,
    var tracer: Boolean,
    var prices: List<AmmoPriceModel>,
    var _id: String,
    var name: String,
    var armor: String,
    var image: String,
    var tradeups: List<AmmoTradeup>,
    var caliber: String = "",
    val armor_damage: Int = 0,
    val bullets: Int = 1
) {
    fun getAccuracy(): String {
        return "$accuracy%"
    }


    fun getSubtitle(): String {
        return if (prices.isEmpty()) {
            return if (!tradeups.isNullOrEmpty()) {
                var tradeString = ""
                for (trade in tradeups) {
                    tradeString += if (tradeups.indexOf(trade) == 0) {
                        trade.toString()
                    } else {
                        "\n$trade"
                    }
                }
                tradeString
            } else {
                "FIR Only"
            }
        } else {
            var priceString = ""
            for (price in prices) {
                priceString += if (prices.indexOf(price) == 0) {
                    price.toString()
                } else {
                    "\n$price"
                }
            }
            priceString
        }
    }

    fun getColor(armorClass: Int): Int {
        if (armor == "------") { return android.R.color.transparent }
        return when (armor[armorClass - 1].toString()) {
            "0" -> R.color.md_red_A200
            "1" -> R.color.md_deep_orange_A200
            "2" -> R.color.md_orange_A200
            "3" -> R.color.md_amber_A200
            "4" -> R.color.md_yellow_A200
            "5" -> R.color.md_light_green_A200
            "6" -> R.color.md_green_A200
            else -> android.R.color.transparent
        }
    }
}

@Serializable
data class AmmoPriceModel (
    var value: Double,
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

@Serializable
data class AmmoTradeup (
    var _id: String,
    var trader: String,
    var level: Int
) {
    override fun toString(): String {
        val format = DecimalFormat("###.##")
        return "${level.getTraderLevel()} $trader Tradeup"
    }
}
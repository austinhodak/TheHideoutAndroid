package com.austinhodak.thehideout.viewmodels.models

import android.util.Log
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.getCurrency
import com.austinhodak.thehideout.getTraderLevel
import java.text.DecimalFormat

data class AmmoModel (
    var description: String,
    var weight: Double,
    var velocity: Int,
    var damage: Int,
    var penetration: Int,
    var recoil: Int,
    var accuracy: Int,
    var tracer: Boolean,
    var prices: List<AmmoPriceModel>,
    var _id: String,
    var name: String,
    var armor: String,
    var image: String
) {
    fun getSubtitle(): String {
        return if (prices.isNullOrEmpty()) {
            "Flea Market or Loot Only"
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
        Log.d("TEST", armor[armorClass - 1] + "")
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
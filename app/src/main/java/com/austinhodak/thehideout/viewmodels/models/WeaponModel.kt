package com.austinhodak.thehideout.viewmodels.models

import android.content.Context
import com.austinhodak.thehideout.ammunition.AmmoHelper
import com.austinhodak.thehideout.getCurrency
import com.austinhodak.thehideout.getTraderLevel
import kotlinx.serialization.Serializable
import java.text.DecimalFormat

@Serializable
data class WeaponModel(
    var grid: WeaponGrid,
    var firemodes: WeaponFiremodes,
    var recoil: WeaponRecoil,
    var description: String,
    var weight: Double?,
    var _id: String,
    var name: String,
    var `class`: String,
    var image: String,
    var rpm: Long,
    var range: Long,
    var ergonomics: Long,
    var accuracy: Double,
    var velocity: Double,
    var fields: List<WeaponField>,
    var calibre: String,
    var __v: Int,
    var builds: List<WeaponBuilds>,
    var wiki: String? = ""
) {
    fun getFireModes(): String {
        return if (firemodes.single) {
            "S"
        } else  if (firemodes.burst) {
            "B"
        } else if (firemodes.auto) {
            "A"
        } else if (firemodes.single && firemodes.burst) {
            "S/B"
        } else if (firemodes.single && firemodes.auto) {
            "S/A"
        } else if (firemodes.burst && firemodes.auto) {
            "B/A"
        } else if (firemodes.single && firemodes.burst && firemodes.auto) {
            "S/B/A"
        } else {
            ""
        }
    }

    fun getCaliber(context: Context): CaliberModel {
        return AmmoHelper.getCaliberByID(context, calibre)!!
    }
}

@Serializable
data class WeaponBuilds (
    var attachments: List<String>,
    var prices: List<WeaponPrices>,
    var tradeups: List<WeaponTradeups>,
    var name: String,
    var image: String,
    var _id: String
) {

    @Serializable
    data class WeaponPrices(
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
    data class WeaponTradeups(
        var items: List<String>,
        var _id: String,
        var trader: String,
        var level: Int
    ) {
        override fun toString(): String {
            val format = DecimalFormat("###.##")
            return "${level.getTraderLevel()} $trader Tradeup"
        }
    }

    override fun toString(): String {
        var string = ""
        for (i in prices) {
            string += "\n$i"
        }

        for (i in tradeups) {
            string += "\n$i"
        }

        return string.trim()
    }

    fun getBasePurchase(): String {
        var string = ""
        val basePrice = prices.firstOrNull()
        val baseTrade = tradeups.firstOrNull()

        if (basePrice != null && baseTrade != null) {
            string += basePrice.toString()
            string += "\n$baseTrade"
        } else if (basePrice != null && baseTrade == null) {
            string += basePrice.toString()
        } else if (basePrice == null && baseTrade != null) {
            string += baseTrade.toString()
        }

        return string
    }
}

@Serializable
data class WeaponField(
    var vital: Boolean,
    var attachments: List<String>,
    var _id: String,
    var name: String
)

@Serializable
data class WeaponRecoil(
    var horizontal: Long,
    var vertical: Long
) {
    override fun toString(): String {
        return (horizontal + vertical).toString()
    }
}

@Serializable
data class WeaponFiremodes(
    var single: Boolean,
    var burst: Boolean,
    var auto: Boolean
)

@Serializable
data class WeaponGrid(
    var x: Int,
    var y: Int
)
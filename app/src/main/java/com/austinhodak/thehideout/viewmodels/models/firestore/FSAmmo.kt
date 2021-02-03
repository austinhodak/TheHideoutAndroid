package com.austinhodak.thehideout.viewmodels.models

import com.austinhodak.thehideout.*
import com.austinhodak.thehideout.calculator.models.CAmmo
import com.google.firebase.firestore.IgnoreExtraProperties
import java.text.DecimalFormat

@IgnoreExtraProperties
data class FSAmmo (
    var description: String? = null,
    var weight: Double? = null,
    var velocity: Int? = null,
    var damage: Int = 0,
    var penetration: Int = 0,
    var recoil: Double? = null,
    var accuracy: Double? = null,
    var tracer: Boolean? = null,
    var prices: List<AmmoPriceModel>? = null,
    var _id: String? = null,
    var name: String? = null,
    var armor: String? = null,
    var image: String? = null,
    var tradeups: List<AmmoTradeup>? = null,
    var caliber: String = "",
    val armor_damage: Int = 0,
    val bullets: Int = 1
) : RecyclerItem {

    fun getAccuracyString(): String {
        return "$accuracy%"
    }

    fun getURL(): String {
        return "https://www.eftdb.one/static/item/thumb/$image"
    }

    fun getCAmmo(): CAmmo {
        return CAmmo(bullets = bullets,
            damage = damage.toDouble(),
            penetration = penetration.toDouble(),
            armorDamage = armor_damage.toDouble())
    }

    fun getSubtitle(): String {
        return if (prices?.isEmpty() == true) {
            return if (!tradeups.isNullOrEmpty()) {
                var tradeString = ""
                for (trade in tradeups!!) {
                    tradeString += if (tradeups!!.indexOf(trade) == 0) {
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
            for (price in prices!!) {
                priceString += if (prices!!.indexOf(price) == 0) {
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
        return when (armor!![armorClass - 1].toString()) {
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

    override val layoutId: Int
        get() = R.layout.ammo_list_item_small

    override val variableId: Int
        get() = BR.ammo

    override val dataToBind: Any
        get() = this

    override val id: String
        get() = this._id!!

    data class AmmoPriceModel (
        var value: Double? = null,
        var _id: String? = null,
        var trader: String? = null,
        var currency: String? = null,
        var level: Int? = null
    ) {
        override fun toString(): String {
            val format = DecimalFormat("###.##")
            return "${level?.getTraderLevel()} $trader ${currency?.getCurrency()}${format.format(value)}"
        }
    }

    data class AmmoTradeup (
        var _id: String? = null,
        var trader: String? = null,
        var level: Int? = null
    ) {
        override fun toString(): String {
            val format = DecimalFormat("###.##")
            return "${level?.getTraderLevel()} $trader Tradeup"
        }
    }
}


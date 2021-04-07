package com.austinhodak.thehideout.ammunition.models

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.calculator.models.CAmmo
import com.austinhodak.thehideout.databinding.ItemAmmoBinding
import com.austinhodak.thehideout.flea_market.models.FleaItem
import com.austinhodak.thehideout.getPrice
import com.austinhodak.thehideout.getTraderLevel
import com.bumptech.glide.Glide
import com.google.firebase.firestore.IgnoreExtraProperties
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import java.text.DecimalFormat

data class Ammo (
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
    val bullets: Int? = null,
    val flea_uid: String? = null
) : AbstractBindingItem<ItemAmmoBinding>() {

    override fun bindView(binding: ItemAmmoBinding, payloads: List<Any>) {
        binding.ammo = this
    }

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ItemAmmoBinding {
        return ItemAmmoBinding.inflate(inflater, parent, false)
    }

    override val type: Int
        get() = R.id.fast_adapter_calc_ammo

    fun getAccuracyString(): String {
        return "$accuracy%"
    }

    fun getFleaMarketItem(value: List<FleaItem>?): FleaItem? {
        return value?.find { it.uid == flea_uid }
    }

    fun getURL(): String {
        //If image URL is whole URL return that, otherwise return appended URL.
        if (image?.contains("https://") == true) return image as String
        return "https://www.eftdb.one/static/item/thumb/$image"
    }

    fun getCAmmo(): CAmmo {
        val b = bullets ?: 1
        return CAmmo(
            bullets = b,
            damage = damage.toDouble() * b,
            penetration = penetration.toDouble(),
            armorDamage = armor_damage.toDouble() * b / 100
        )
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

    fun isArmorEmpty(): Boolean {
        return armor == "------"
    }

    fun getColor(armorClass: Int): Int {
        if (armor == "------") { return android.R.color.transparent }
        return when (armor!![armorClass - 1].toString()) {
            "0" -> R.color.ammoChart0 //CE0B04
            "1" -> R.color.ammoChart1 //DC3B07
            "2" -> R.color.ammoChart2 //EA6C0A
            "3" -> R.color.ammoChart3 //F99D0E
            "4" -> R.color.ammoChart4 //C0B825
            "5" -> R.color.ammoChart5 //86D43D
            "6" -> R.color.ammoChart6 //4BF056
            else -> android.R.color.transparent
        }
    }

    fun getArmorChartText(armorClass: Int): String {
        if (armor == "------") { return "" }
        return when (armor!![armorClass - 1].toString()) {
            "0" -> " • Pointless • 20+ Shots"
            "1" -> " • It's Possible • 13 to 20 Shots"
            "2" -> " • Magdump Only • 9 to 13 Shots"
            "3" -> " • Slightly Effective • 5 to 9 Shots"
            "4" -> " • Effective • 3 to 5 Shots"
            "5" -> " • Very Effective • 1 to 3 Shots"
            "6" -> " • Basically Ignores • <1 Shot"
            else -> ""
        }
    }

    @IgnoreExtraProperties
    data class AmmoPriceModel (
        var value: Double? = null,
        var _id: String? = null,
        var trader: String? = null,
        var currency: String? = null,
        var level: Int? = null
    ) {
        override fun toString(): String {
            val format = DecimalFormat("###.##")
            return if (value!! > 0)  {
                "$trader ${level?.getTraderLevel()} ${value?.getPrice(currency ?: "₽")}"
            } else {
                "\"$trader ${level?.getTraderLevel()} ???"
            }
            /*return if (currency?.getCurrency() == "$") {
                "$trader ${level?.getTraderLevel()} ${currency?.getCurrency()}${format.format(value)}"
            } else {
                "$trader ${level?.getTraderLevel()} ${format.format(value)}${currency?.getCurrency()}"
            }*/
        }

        fun getTraderString(): String {
            return "$trader ${level?.getTraderLevel()} "
        }

        fun getPrice(): String {
            if (value == null || value == 0.00) return "???"
            return value!!.getPrice(currency ?: "₽")
        }
    }

    @IgnoreExtraProperties
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

    companion object {
        @JvmStatic @BindingAdapter("imageUrl")
        fun loadImage(view: ImageView, url: String) {
            Glide.with(view.context).load(url).placeholder(R.drawable.icons8_ammo_100).into(view)
        }

        @JvmStatic @BindingAdapter("armorColor")
        fun setColor(view: View, int: Int) {
            view.setBackgroundResource(int)
        }
    }
}


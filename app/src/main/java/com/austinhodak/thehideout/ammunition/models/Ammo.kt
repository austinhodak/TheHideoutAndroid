package com.austinhodak.thehideout.ammunition.models

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.calculator.models.CAmmo
import com.austinhodak.thehideout.databinding.ItemAmmoBinding
import com.austinhodak.thehideout.getCurrency
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
    val bullets: Int = 1
) : AbstractBindingItem<ItemAmmoBinding>() {

    override fun bindView(binding: ItemAmmoBinding, payloads: List<Any>) {
        binding.ammo = this
    }

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ItemAmmoBinding {
        return ItemAmmoBinding.inflate(inflater, parent, false)
    }

    override val type: Int
        get() = R.id.fast_adapter_id

    fun getAccuracyString(): String {
        return "$accuracy%"
    }

    fun getURL(): String {
        //If image URL is whole URL return that, otherwise return appended URL.
        if (image?.contains("https://") == true) return image as String
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

    fun isArmorEmpty(): Boolean {
        return armor == "------"
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
            return "${level?.getTraderLevel()} $trader ${currency?.getCurrency()}${format.format(value)}"
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


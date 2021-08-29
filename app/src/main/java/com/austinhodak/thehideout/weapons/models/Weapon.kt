package com.austinhodak.thehideout.weapons.models

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.austinhodak.tarkovapi.utils.getTraderLevel
import com.austinhodak.thehideout.ammunition.AmmoHelper
import com.austinhodak.thehideout.databinding.ItemWeaponBinding
import com.austinhodak.thehideout.utils.getCurrency
import com.bumptech.glide.Glide
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import java.text.DecimalFormat

data class Weapon (
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
) : AbstractBindingItem<ItemWeaponBinding>() {

    override val type: Int
        get() = Math.random().toInt()

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ItemWeaponBinding {
        return ItemWeaponBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: ItemWeaponBinding, payloads: List<Any>) {
        //binding.weapon = this
    }

    fun getImageURL(): String {
        return "https://www.eftdb.one/static/item/full/${image}"
    }

    fun getBuildsText(): String {
        return if (builds.isNotEmpty()) builds.first().getBasePurchase() else ""
    }

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

    fun getCaliber(): AmmoHelper.Caliber {
        return AmmoHelper.getCaliberByID(calibre) ?: AmmoHelper.Caliber ("", "", "")
    }

    data class WeaponBuilds (
        var attachments: List<String>,
        var prices: List<WeaponPrices>,
        var tradeups: List<WeaponTradeups>,
        var name: String,
        var image: String,
        var _id: String
    ) {

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

    data class WeaponField(
        var vital: Boolean,
        var attachments: List<String>,
        var _id: String,
        var name: String
    )

    data class WeaponRecoil(
        var horizontal: Long,
        var vertical: Long
    ) {
        override fun toString(): String {
            return (horizontal + vertical).toString()
        }
    }

    data class WeaponFiremodes(
        var single: Boolean,
        var burst: Boolean,
        var auto: Boolean
    )

    data class WeaponGrid(
        var x: Int,
        var y: Int
    )

    companion object {
        @JvmStatic @BindingAdapter("weaponImageURL")
        fun loadImage(view: ImageView, url: String?) {
            Glide.with(view.context).load(url).into(view)
        }
    }
}
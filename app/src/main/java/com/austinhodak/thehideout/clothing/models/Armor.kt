package com.austinhodak.thehideout.clothing.models

import android.view.LayoutInflater
import android.view.ViewGroup
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.calculator.models.CArmor
import com.austinhodak.thehideout.databinding.ItemPickerCalculatorArmorBinding
import com.bumptech.glide.Glide
import com.mikepenz.fastadapter.binding.AbstractBindingItem

data class Armor(
    var description: String,
    var weight: Double,
    var level: Int,
    var hitpoints: Int,
    var movement: Int,
    var turn: Int,
    var ergonomics: Int,
    var zones: List<String>,
    var _id: String,
    var name: String,
    var image: String,
    var material: String,
    var `class`: String,
    var ricochet: String?,
    var grid: Grid,
    var prices: List<Price>,
    var tradeups: List<Tradeup>,
    var fields: List<Field>,
    var internal: Int,
    var blunt: Double,
    var resistance: Double,
    var destructibility: Double,
    var cArmor: CArmor? = null
) : AbstractBindingItem<ItemPickerCalculatorArmorBinding>() {

    override val type: Int
        get() = R.id.fast_adapter_calc_armor

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ItemPickerCalculatorArmorBinding {
        return ItemPickerCalculatorArmorBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: ItemPickerCalculatorArmorBinding, payloads: List<Any>) {
        Glide.with(binding.root.context).load(getImageURL()).into(binding.imageView5)
        binding.item = this
    }

    data class Field(
        var vital: Boolean,
        var _id: String,
        var name: String
    )

    fun getImageURL(): String {
        return if (image.contains("https")) return image else "https://eftdb.one/static/item/thumb/$image"
    }

    fun resetDurability() {
        cArmor = CArmor(
            `class` = level,
            bluntThroughput = blunt,
            durability = hitpoints.toDouble(),
            maxDurability = hitpoints.toDouble(),
            resistance = resistance,
            destructibility = destructibility,
            zones = zones
        )
    }

    fun getArmor(durability: Double? = null): CArmor {
        if (cArmor == null) {
            cArmor = CArmor(
                `class` = level,
                bluntThroughput = blunt,
                durability = durability ?: hitpoints.toDouble(),
                maxDurability = hitpoints.toDouble(),
                resistance = resistance,
                destructibility = destructibility,
                zones = zones
            )
        }

        return cArmor as CArmor
    }

    fun getCArmor(durability: Double? = null): CArmor {
        return CArmor(
            `class` = level,
            bluntThroughput = blunt,
            durability = durability ?: hitpoints.toDouble(),
            maxDurability = hitpoints.toDouble(),
            resistance = resistance,
            destructibility = destructibility,
            zones = zones
        )
    }


    fun getPickerSubtitle(): String {
        return "Class $level • $material"
    }
}
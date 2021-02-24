package com.austinhodak.thehideout.clothing.models

import android.view.LayoutInflater
import android.view.ViewGroup
import com.austinhodak.thehideout.calculator.models.CArmor
import com.austinhodak.thehideout.databinding.CalculatorListItemArmorBinding
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
    var ricochet: String,
    var grid: Grid,
    var prices: List<Price>,
    var tradeups: List<Tradeup>,
    var fields: List<Field>,
    var internal: Int,
    var blunt: Double,
    var resistance: Double,
    var destructibility: Double,
    var cArmor: CArmor? = null
) : AbstractBindingItem<CalculatorListItemArmorBinding>() {

    override val type: Int
        get() = internal

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): CalculatorListItemArmorBinding {
        return CalculatorListItemArmorBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: CalculatorListItemArmorBinding, payloads: List<Any>) {
        Glide.with(binding.root.context).load("https://eftdb.one/static/item/thumb/$image").into(binding.imageView5)
        binding.item = this
    }

    data class Field(
        var vital: Boolean,
        var _id: String,
        var name: String
    )

    fun resetDurability() {
        cArmor = CArmor(
            `class` = level,
            bluntThroughput = blunt,
            durability = hitpoints.toDouble(),
            maxDurability = hitpoints.toDouble(),
            resistance = resistance,
            destructibility = destructibility
        )
    }

    fun getArmor(): CArmor {
        if (cArmor == null) {
            cArmor = CArmor(
                `class` = level,
                bluntThroughput = blunt,
                durability = hitpoints.toDouble(),
                maxDurability = hitpoints.toDouble(),
                resistance = resistance,
                destructibility = destructibility
            )
        }

        return cArmor as CArmor
    }

    fun getSubtitle(): String {
        var string = ""
        for (i in prices) {
            string += "\n$i"
        }

        for (i in tradeups) {
            string += "\n$i"
        }

        return string.trim()
    }

    fun getPickerSubtitle(): String {
        return "Class $level • $material"
    }
}
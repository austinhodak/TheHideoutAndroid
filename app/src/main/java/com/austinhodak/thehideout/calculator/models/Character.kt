package com.austinhodak.thehideout.calculator.models

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.databinding.ItemPickerCalculatorCharacterBinding
import com.mikepenz.fastadapter.binding.AbstractBindingItem

data class Character(
    val name: String,
    val health: Health,
    val image: String,
    val c_type: String,
    val spawn_chance: Int? = null
) : AbstractBindingItem<ItemPickerCalculatorCharacterBinding>() {

    override val type: Int
        get() = R.id.fast_adapter_calc_character

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ItemPickerCalculatorCharacterBinding {
        return ItemPickerCalculatorCharacterBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: ItemPickerCalculatorCharacterBinding, payloads: List<Any>) {
        binding.character = this

        when (c_type) {
            "boss" -> {
                binding.calcPickerCharacterIcon.setImageResource(R.drawable.icons8_crown_96)
                binding.calcPickerCharacterIcon.imageTintList = ColorStateList.valueOf(binding.calcPickerCharacterIcon.context.resources.getColor(R.color.md_amber_500))
            }
            "raider",
            "guard" -> {
                binding.calcPickerCharacterIcon.setImageResource(R.drawable.icons8_defense_96)
                binding.calcPickerCharacterIcon.imageTintList = ColorStateList.valueOf(binding.calcPickerCharacterIcon.context.resources.getColor(R.color.md_brown_500))
            }
            "player" -> {
                binding.calcPickerCharacterIcon.setImageResource(R.drawable.icons8_army_star_96)
                binding.calcPickerCharacterIcon.imageTintList = ColorStateList.valueOf(binding.calcPickerCharacterIcon.context.resources.getColor(R.color.md_grey_400))
            }
            else -> {
                binding.calcPickerCharacterIcon.setImageDrawable(null)
            }
        }
    }

    data class Health(
        val head: Int,
        val thorax: Int,
        val stomach: Int,
        val arms: Int,
        val legs: Int
    ) {
        override fun toString(): String {
            return (head + thorax + stomach + (arms * 2) + (legs * 2)).toString()
        }
    }
}
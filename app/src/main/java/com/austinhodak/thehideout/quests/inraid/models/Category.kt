package com.austinhodak.thehideout.quests.inraid.models

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.databinding.ItemInRaidCategoryBinding
import com.austinhodak.thehideout.databinding.ItemInRaidObjectiveBinding
import com.austinhodak.thehideout.quests.models.Quest
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.binding.AbstractBindingItem

data class Category(
    var title: String,
    @DrawableRes var icon: Int,
    var items: List<Quest>,
    var objectiveType: String,
    var map: String
) : AbstractBindingItem<ItemInRaidCategoryBinding>() {

    override val type: Int
        get() = R.id.fast_adapter_id

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ItemInRaidCategoryBinding {
        return ItemInRaidCategoryBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: ItemInRaidCategoryBinding, payloads: List<Any>) {
        binding.category = this

        val itemAdapter = ItemAdapter<Objective>()
        val adapter = FastAdapter.with(itemAdapter)

        binding.inRaidCategoryRV.layoutManager = LinearLayoutManager(binding.root.context)
        binding.inRaidCategoryRV.adapter = adapter

        val list: MutableList<Objective> = ArrayList()

        items.forEach { quest ->
            quest.objectives.filter { it.type.toUpperCase() == objectiveType }.filter { it.location == "Any" || it.location == map }.forEach { obj ->
                val key = quest.objectives.find { it.type == "key" && it.location == obj.location }
                if (key != null) {
                    list.add(Objective(obj, subtitle = "${key.target} needed"))
                } else if (!obj.with.isNullOrEmpty()) {
                    list.add(Objective(obj, obj.with.joinToString(prefix = "With ", separator = " & ")))
                } else if (!obj.hint.isNullOrEmpty()) {
                    list.add(Objective(obj, "Location: ${obj.hint}"))
                } else {
                    list.add(Objective(obj))
                }
            }
        }

        itemAdapter.add(list)
    }

    companion object {
        @JvmStatic @BindingAdapter("category:icon")
        fun loadImage(view: ImageView, int: Int?) {
            if (int != null)
                view.setImageResource(int)
        }
    }

    data class Objective (
        var objective: Quest.QuestObjectives,
        var subtitle: String? = null
    ) : AbstractBindingItem<ItemInRaidObjectiveBinding>() {
        override val type: Int
            get() = R.id.fast_adapter_key_id

        override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ItemInRaidObjectiveBinding {
            return ItemInRaidObjectiveBinding.inflate(inflater, parent, false)
        }

        override fun bindView(binding: ItemInRaidObjectiveBinding, payloads: List<Any>) {
            binding.item = this

            binding.inRaidObjectiveTitle.apply {
                text = if (objective.type == "mark" && objective.tool != null) {
                    "${objective.getNumberString()}${objective.target} with ${objective.tool}".split(" ").joinToString(" ") { it.capitalize() }
                } else "${objective.getNumberString()}${objective.target}".split(" ").joinToString(" ") { it.capitalize() }
            }

            if (subtitle != null) {
                binding.inRaidObjectiveSubtitle.apply {
                    text = subtitle
                    visibility = View.VISIBLE
                }
            }
        }
    }
}
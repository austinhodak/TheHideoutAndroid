package com.austinhodak.thehideout.quests.inraid.models

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.databinding.ItemStartRaidMapBinding
import com.austinhodak.thehideout.utils.Map
import com.mikepenz.fastadapter.binding.AbstractBindingItem

data class Map(
    val map: Map
) : AbstractBindingItem<ItemStartRaidMapBinding>() {

    override val type: Int
        get() = R.id.fast_adapter_id

    override fun bindView(binding: ItemStartRaidMapBinding, payloads: List<Any>) {
        binding.map = this
    }

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ItemStartRaidMapBinding {
        return ItemStartRaidMapBinding.inflate(inflater, parent, false)
    }

    companion object {
        @JvmStatic @BindingAdapter("map:icon")
        fun loadImage(view: ImageView, int: Int?) {
            if (int != null)
                view.setImageResource(int)
        }
    }
}
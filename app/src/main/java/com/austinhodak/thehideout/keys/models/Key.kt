package com.austinhodak.thehideout.keys.models

import android.view.LayoutInflater
import android.view.ViewGroup
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.databinding.ItemKeyBinding
import com.austinhodak.thehideout.log
import com.austinhodak.thehideout.userRef
import com.mikepenz.fastadapter.binding.AbstractBindingItem

data class Key(
    val icon: String,
    var name: String,
    val link: String,
    val map: String,
    val location: String,
    val door: String,
    val details: List<String>,
    val _id: String,
    var have: Boolean
) :  AbstractBindingItem<ItemKeyBinding>() {

    override val type: Int
        get() = R.id.fast_adapter_key_id

    override fun bindView(binding: ItemKeyBinding, payloads: List<Any>) {
        binding.key = this
    }

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ItemKeyBinding {
        return ItemKeyBinding.inflate(inflater, parent, false)
    }

    fun getDetailsList(): String {
        return details.joinToString(" ")
    }

    fun toggleHaveStatus() {
        log("toggle_key", _id, name, "key")
        userRef("keys/have/$_id").setValue(!have)
        have = !have
    }
}


package com.austinhodak.thehideout.viewmodels.models

import android.view.LayoutInflater
import android.view.ViewGroup
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.databinding.KeyListItemBinding
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
) :  AbstractBindingItem<KeyListItemBinding>() {

    override val type: Int
        get() = R.id.fast_adapter_key_id

    override fun bindView(binding: KeyListItemBinding, payloads: List<Any>) {
        binding.key = this
    }

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): KeyListItemBinding {
        return KeyListItemBinding.inflate(inflater, parent, false)
    }

    fun getDetailsList(): String {
        return details.joinToString(" ")
    }

    fun toggleHaveStatus() {
        userRef("keys/have/$_id").setValue(!have)
        have = !have
    }
}


package com.austinhodak.thehideout.viewmodels.models

import com.austinhodak.thehideout.BR
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.RecyclerItem
import com.austinhodak.thehideout.userRef

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
) : RecyclerItem {

    fun getDetailsList(): String {
        return details.joinToString(" ")
    }

    fun toggleHaveStatus() {
        userRef("keys/have/$_id").setValue(!have)
        have = !have
    }

    override val layoutId: Int
        get() = R.layout.key_list_item

    override val variableId: Int
        get() = BR.key

    override val dataToBind: Any
        get() = this

    override val id: String
        get() = this._id
}


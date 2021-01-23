package com.austinhodak.thehideout.viewmodels.models

import com.austinhodak.thehideout.BR
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.RecyclerItem

data class CaliberModel (
    var description: String,
    var _id: String,
    var name: String,
    var image: String,
    var ammo: List<AmmoModel>,
    var long_name: String
) : RecyclerItem {

    fun getTotalAmmo(): String {
        return "${ammo.size} Total"
    }

    override val layoutId: Int
        get() = R.layout.ammo_category_item

    override val variableId: Int
        get() = BR.caliber

    override val dataToBind: Any
        get() = this

    override val id: String
        get() = this._id

}
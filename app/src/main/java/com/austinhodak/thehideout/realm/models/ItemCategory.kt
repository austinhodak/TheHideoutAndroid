package com.austinhodak.thehideout.realm.models

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class ItemCategory : RealmObject {
    @PrimaryKey
    var id: String = ""
    var name: String = ""
    var normalizedName: String = ""
    var parent: ItemCategory? = null
    var children: List<ItemCategory>? = null
}
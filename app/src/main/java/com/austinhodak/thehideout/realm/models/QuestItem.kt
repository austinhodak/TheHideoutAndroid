package com.austinhodak.thehideout.realm.models

import com.austinhodak.thehideout.QuestItemsQuery
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class QuestItem : RealmObject {
    @PrimaryKey
    var id: String = ""
    var name: String? = null
    var shortName: String? = null
    var description: String? = null
    var normalizedName: String? = null
    var width: Int = 0
    var height: Int = 0
    var iconLink: String? = null
    var gridImageLink: String? = null
    var baseImageLink: String? = null
    var inspectImageLink: String? = null
    var image512pxLink: String? = null
    var image8xLink: String? = null
}

fun QuestItemsQuery.Data.QuestItem.toRealm(realm: MutableRealm): QuestItem? {
    val i = this
    return i.id?.let {
        QuestItem().apply {
            id = i.id
            name = i.name
            shortName = i.shortName
            description = i.description
            normalizedName = i.normalizedName
            width = i.width ?: 1
            height = i.height ?: 1
            iconLink = i.iconLink
            gridImageLink = i.gridImageLink
            baseImageLink = i.baseImageLink
            inspectImageLink = i.inspectImageLink
            image512pxLink = i.image512pxLink
            image8xLink = i.image8xLink
        }
    }
}
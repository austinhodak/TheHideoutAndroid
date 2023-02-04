package com.austinhodak.thehideout.realm.models

import com.austinhodak.thehideout.BartersQuery
import com.austinhodak.thehideout.realm.converters.findObjectById
import com.austinhodak.thehideout.fragment.TaskItem
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class Barter : RealmObject {
    @PrimaryKey
    var id: String = ""
    var trader: Trader? = null
    var level: Int = 0
    var taskUnlock: Task? = null
    var requiredItems: RealmList<Item.ContainedItem> = realmListOf()
    var rewardItems: RealmList<Item.ContainedItem> = realmListOf()
}

fun BartersQuery.Data.Barter.toRealm(realm: MutableRealm): Barter {
    val b = this
    return Barter().apply {
        id = b.id
        trader = findObjectById(realm, b.trader.id)
        level = b.level
        taskUnlock = b.taskUnlock?.id?.let { findObjectById(realm, it) }
        b.requiredItems.filterNotNull().map {
            it.toRealm(realm)
        }.let { requiredItems = it.toRealmList() }
    }
}

fun TaskItem.toRealm(realm: MutableRealm): Item.ContainedItem {
    val t = this
    return Item.ContainedItem().apply {
        item = findObjectById(realm, t.item.id)
        count = t.count
        t.attributes?.filterNotNull().let {
            attributes = it?.map {
                Item.ContainedItem.ItemAttribute().apply {
                    name = it.name
                    value = it.value ?: ""
                    type = it.type
                }
            }?.toRealmList() ?: realmListOf()
        }
    }
}
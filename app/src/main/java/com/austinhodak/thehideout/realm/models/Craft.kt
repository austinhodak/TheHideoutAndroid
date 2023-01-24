package com.austinhodak.thehideout.realm.models

import com.austinhodak.thehideout.CraftsQuery
import com.austinhodak.thehideout.realm.converters.findObjectById
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class Craft : RealmObject {
    @PrimaryKey
    var id: String = ""
    var station: HideoutStation? = null
    var level: Int? = null
    var taskUnlock: Task? = null
    var duration: Int? = null
    var requiredItems: RealmList<Item.ContainedItem> = realmListOf()
    var requiredQuestItems: RealmList<QuestItem> = realmListOf()
    var rewardItems: RealmList<Item.ContainedItem> = realmListOf()
}

fun CraftsQuery.Data.Craft.toRealm(realm: MutableRealm): Craft {
    val c = this
    return Craft().apply {
        id = c.id
        station = c.station.id.let { findObjectById(realm, id) }
        level = c.level
        taskUnlock = c.taskUnlock?.id?.let { findObjectById(realm, it) }
        duration = c.duration
        c.requiredItems.filterNotNull().map { s ->
            s.toRealm(realm)
        }.let { requiredItems = it.toRealmList() }
        c.rewardItems.filterNotNull().map { s ->
            s.toRealm(realm)
        }.let { rewardItems = it.toRealmList() }
        c.requiredQuestItems.filterNotNull().mapNotNull { s ->
            s.id?.let { findObjectById<QuestItem>(realm, it) }
        }.let { requiredQuestItems = it.toRealmList() }
    }
}
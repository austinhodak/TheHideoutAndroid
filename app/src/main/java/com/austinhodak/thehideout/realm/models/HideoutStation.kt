package com.austinhodak.thehideout.realm.models

import com.austinhodak.thehideout.HideoutQuery
import com.austinhodak.thehideout.data.findObjectById
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.EmbeddedRealmObject
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class HideoutStation : RealmObject {
    @PrimaryKey
    var id: String = ""
    var name: String = ""
    var normalizedName: String = ""
    var levels: RealmList<HideoutStationLevel> = realmListOf()
    var crafts: RealmList<Craft> = realmListOf()

    class HideoutStationLevel : EmbeddedRealmObject {
        var level: Int = 0
        var constructionTime: Int = 0
        var description: String = ""
        var itemRequirements: RealmList<RequirementItem> = realmListOf()
        var stationLevelRequirements: RealmList<RequirementHideoutStationLevel> = realmListOf()
        var skillRequirements: RealmList<RequirementSkill> = realmListOf()
        var traderRequirements: RealmList<RequirementTrader> = realmListOf()
        var crafts: RealmList<Craft> = realmListOf()
    }
}

class RequirementItem : EmbeddedRealmObject {
    var item: Item? = null
    var count: Int = 0
    var attributes: RealmList<Item.ContainedItem.ItemAttribute> = realmListOf()
}

class RequirementHideoutStationLevel : EmbeddedRealmObject {
    var item: Item? = null
    var station: HideoutStation? = null
    var level: Int = 0
}

class RequirementSkill : EmbeddedRealmObject {
    var name: String = ""
    var level: Int = 0
}

class RequirementTrader : EmbeddedRealmObject {
    var trader: Trader? = null
    var level: Int = 0
}

fun HideoutQuery.Data.HideoutStation.toRealm(realm: MutableRealm): HideoutStation {
    val s = this
    return HideoutStation().apply {
        id = s.id
        name = s.name
        normalizedName = s.normalizedName
        s.levels.filterNotNull().map {
            HideoutStation.HideoutStationLevel().apply {
                level = it.level
                constructionTime = it.constructionTime
                description = it.description
                it.itemRequirements.filterNotNull().map {
                    RequirementItem().apply {
                        item = it.item.id.let { findObjectById(realm, it) }
                        count = it.count
                        it.attributes?.filterNotNull().let {
                            it?.toRealmList()
                        }
                    }
                }.let { itemRequirements = it.toRealmList() }
                it.stationLevelRequirements.filterNotNull().map {
                    RequirementHideoutStationLevel().apply {
                        station = it.station.id.let {
                            findObjectById(realm, it)
                        }
                        level = it.level
                    }
                }.let { stationLevelRequirements = it.toRealmList() }
                it.skillRequirements.filterNotNull().map {
                    RequirementSkill().apply {
                        name = it.name
                        level = it.level
                    }
                }.let { skillRequirements = it.toRealmList() }
                it.traderRequirements.filterNotNull().map {
                    RequirementTrader().apply {
                        trader = it.trader.id.let { findObjectById(realm, it) }
                        level = it.level
                    }
                }.let { traderRequirements = it.toRealmList() }
            }
        }.let { levels = it.toRealmList() }
        crafts = s.crafts.filterNotNull().mapNotNull {
            findObjectById<Craft>(realm, it.id)
        }.toRealmList()
    }
}
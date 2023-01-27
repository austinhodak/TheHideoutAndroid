package com.austinhodak.thehideout.realm.models

import com.austinhodak.thehideout.MapsQuery
import com.austinhodak.thehideout.realm.converters.findObjectById
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.EmbeddedRealmObject
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class Map : RealmObject {
    @PrimaryKey
    var id: String = ""
    var name: String = ""
    var normalizedName: String = ""
    var wiki: String? = null
    var description: String? = null
    var enemies: RealmList<String> = realmListOf()
    var raidDuration: Int? = null
    var players: String? = null
    var bosses: RealmList<BossSpawn> = realmListOf()
    var nameId: String? = null

    class BossSpawn : EmbeddedRealmObject {
        var boss: MobInfo? = null
        var spawnChance: Double = 0.0
        var spawnLocations: RealmList<BossSpawnLocation> = realmListOf()
        var escorts: RealmList<BossEscort> = realmListOf()
        var spawnTime: Int? = null
        var spawnTimeRandom: Boolean? = null
        var spawnTrigger: String? = null

        class BossSpawnLocation : EmbeddedRealmObject {
            var name: String = ""
            var chance: Double = 0.0
        }

        class BossEscort : EmbeddedRealmObject {
            var boss: MobInfo? = null
            var amount: RealmList<BossEscortAmount> = realmListOf()

            class BossEscortAmount: EmbeddedRealmObject {
                var count: Int = 0
                var chance: Double = 0.0
            }
        }
    }
}

fun MapsQuery.Data.Map.toRealm(realm: MutableRealm): Map {
    val m = this
    return Map().apply {
        id = m.id
        name = m.name
        normalizedName = m.normalizedName
        wiki = m.wiki
        description = m.description
        enemies = m.enemies?.filterNotNull()?.toRealmList() ?: realmListOf()
        raidDuration = m.raidDuration
        players = m.players
        bosses = m.bosses.filterNotNull().map { b ->
            Map.BossSpawn().apply {
                boss = b.boss.let { findObjectById(realm, it.normalizedName) }
                spawnChance = b.spawnChance
                spawnLocations = b.spawnLocations.filterNotNull()?.map { location ->
                    Map.BossSpawn.BossSpawnLocation().apply {
                        name = location.name
                        chance = location.chance
                    }
                }?.toRealmList() ?: realmListOf()
                escorts = b.escorts.filterNotNull().map { escort ->
                    Map.BossSpawn.BossEscort().apply {
                        boss = findObjectById(realm, escort.boss.normalizedName)
                        amount = escort.amount?.filterNotNull()?.map { amount ->
                            Map.BossSpawn.BossEscort.BossEscortAmount().apply {
                                count = amount.count
                                chance = amount.chance
                            }
                        }?.toRealmList() ?: realmListOf()
                    }
                }.toRealmList()
                spawnTime = b.spawnTime
                spawnTimeRandom = b.spawnTimeRandom
                spawnTrigger = b.spawnTrigger
            }
        }.toRealmList() ?: realmListOf()
        nameId = m.nameId
    }
}
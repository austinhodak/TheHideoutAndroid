package com.austinhodak.thehideout.data.sync

import com.apollographql.apollo3.ApolloClient
import com.austinhodak.thehideout.AmmoQuery
import com.austinhodak.thehideout.BossesQuery
import com.austinhodak.thehideout.QuestItemsQuery
import com.austinhodak.thehideout.TasksQuery
import com.austinhodak.thehideout.TradersQuery
import com.austinhodak.thehideout.apollo.TarkovApiRepository
import com.austinhodak.thehideout.fragment.ItemFragment
import com.austinhodak.thehideout.realm.RealmRepository
import com.austinhodak.thehideout.realm.converters.getApolloData
import com.austinhodak.thehideout.realm.converters.toRealmAmmo
import com.austinhodak.thehideout.realm.converters.toRealmItem
import com.austinhodak.thehideout.realm.models.Item
import com.austinhodak.thehideout.realm.models.asRealmTrader
import com.austinhodak.thehideout.realm.models.toRealm
import com.austinhodak.thehideout.type.ItemType
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class Sync @Inject constructor(
    private val tarkovApiRepository: TarkovApiRepository,
    private val realmRepository: RealmRepository,
    private val realm: Realm,
    private val apollo: ApolloClient
) {
    suspend fun syncItems() = suspendRunCatching{
        val allTarkovItems = tarkovApiRepository.getAllItems(ItemType.any).first()

        realm.writeBlocking {
            allTarkovItems.forEach { tarkovItem ->
                val realmItem = realm.query<Item>("id == '${tarkovItem.id}'").first().find()
                if (realmItem != null) {
                    runBlocking {
                        updateItem(realmItem, tarkovItem, this@writeBlocking)
                    }
                } else {
                    runBlocking {
                        updateItem(null, tarkovItem, this@writeBlocking)
                    }
                }
            }
        }
    }.isSuccess

    suspend fun syncTraders() = suspendRunCatching {
        val traders = getApolloData<TradersQuery.Data.Trader>(apollo)
        traders?.let {
            realm.writeBlocking {
                it.forEach { trader ->
                    runBlocking {
                        copyToRealm(trader.asRealmTrader(this@writeBlocking, apollo), UpdatePolicy.ALL)
                    }
                }
            }
        }
    }.isSuccess

    suspend fun syncTasks() = suspendRunCatching{
        val tasks = getApolloData<TasksQuery.Data.Task>(apollo)
        tasks?.let {
            realm.writeBlocking {
                it.forEach { task ->
                    runBlocking {
                        copyToRealm(task.toRealm(this@writeBlocking), UpdatePolicy.ALL)
                    }
                }
            }
        }
    }.isSuccess

    suspend fun syncOther() = suspendRunCatching{

    }.isSuccess

    suspend fun syncAmmo() = suspendRunCatching{
        val ammo = getApolloData<AmmoQuery.Data.Ammo>(apollo)
        ammo?.let {
            realm.writeBlocking {
                it.forEach { ammo ->
                    runBlocking {
                        copyToRealm(ammo.toRealmAmmo(this@writeBlocking), UpdatePolicy.ALL)
                    }
                }
            }
        }
    }.isSuccess

    suspend fun syncBarters() = suspendRunCatching{
        val barters = getApolloData<com.austinhodak.thehideout.BartersQuery.Data.Barter>(apollo)
        barters?.let {
            realm.writeBlocking {
                it.forEach { barter ->
                    runBlocking {
                        copyToRealm(barter.toRealm(this@writeBlocking), UpdatePolicy.ALL)
                    }
                }
            }
        }
    }.isSuccess

    suspend fun syncHideout() = suspendRunCatching{
        val hideout = getApolloData<com.austinhodak.thehideout.HideoutQuery.Data.HideoutStation>(apollo)
        hideout?.let {
            realm.writeBlocking {
                it.forEach { hideout ->
                    runBlocking {
                        copyToRealm(hideout.toRealm(this@writeBlocking), UpdatePolicy.ALL)
                    }
                }
            }
        }
    }.isSuccess

    suspend fun syncCrafts() = suspendRunCatching {
        val crafts = getApolloData<com.austinhodak.thehideout.CraftsQuery.Data.Craft>(apollo)
        crafts?.let {
            realm.writeBlocking {
                it.forEach { craft ->
                    runBlocking {
                        copyToRealm(craft.toRealm(this@writeBlocking), UpdatePolicy.ALL)
                    }
                }
            }
        }
    }.isSuccess

    suspend fun syncMaps() = suspendRunCatching {
        val maps = getApolloData<com.austinhodak.thehideout.MapsQuery.Data.Map>(apollo)
        maps?.let {
            realm.writeBlocking {
                it.forEach { map ->
                    runBlocking {
                        copyToRealm(map.toRealm(this@writeBlocking), UpdatePolicy.ALL)
                    }
                }
            }
        }
    }.isSuccess

    suspend fun syncMobInfo() = suspendRunCatching {
        val mobInfo = getApolloData<BossesQuery.Data.Boss>(apollo)
        mobInfo?.let {
            realm.writeBlocking {
                it.forEach { mobInfo ->
                    runBlocking {
                        copyToRealm(mobInfo.toRealm(this@writeBlocking), UpdatePolicy.ALL)
                    }
                }
            }
        }
    }.isSuccess

    suspend fun syncQuestItems() = suspendRunCatching {
        val questItems = getApolloData<QuestItemsQuery.Data.QuestItem>(apollo)
        questItems?.let {
            realm.writeBlocking {
                it.forEach { questItem ->
                    runBlocking {
                        val item = questItem.toRealm(this@writeBlocking)
                        item?.let {
                            copyToRealm(it, UpdatePolicy.ALL)
                        }
                    }
                }
            }
        }
    }.isSuccess

    private suspend fun updateItem(realmItem: Item? = null, tarkovItem: ItemFragment, block: MutableRealm) {
        block.copyToRealm(
            tarkovItem.toRealmItem(block, apollo),
            UpdatePolicy.ALL
        )
    }
}

private suspend fun <T> suspendRunCatching(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (cancellationException: CancellationException) {
    throw cancellationException
} catch (exception: Exception) {
    Timber.tag("suspendRunCatching")
        .i(exception, "Failed to evaluate a suspendRunCatchingBlock. Returning failure Result")
    Result.failure(exception)
}
package com.austinhodak.tarkovapi.room

import androidx.work.ListenableWorker
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.austinhodak.tarkovapi.BartersQuery
import com.austinhodak.tarkovapi.CraftsQuery
import com.austinhodak.tarkovapi.ItemsByTypeQuery
import com.austinhodak.tarkovapi.QuestsQuery
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.models.Price
import com.austinhodak.tarkovapi.type.ItemType
import com.austinhodak.tarkovapi.utils.toBarter
import com.austinhodak.tarkovapi.utils.toCraft
import com.austinhodak.tarkovapi.utils.toPricing
import com.austinhodak.tarkovapi.utils.toQuest
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.system.measureTimeMillis

class Updaters (
    val tarkovRepo: TarkovRepo,
    val apolloClient: ApolloClient
) {
    suspend fun updateAll(): List<ListenableWorker.Result> {
        return coroutineScope {
            awaitAll(async {
                updatePricing()
            }, async {
                populateQuests()
            }, async {
                populateCrafts()
            }, async {
                populateBarters()
            })
        }
    }

     suspend fun updatePricing(): ListenableWorker.Result {

        return try {
            val itemDao = tarkovRepo.getItemDao()
            val priceDao = tarkovRepo.getPriceDao()
            val response = apolloClient.query(ItemsByTypeQuery(ItemType.any)).fetchPolicy(FetchPolicy.NetworkFirst).execute()
            val items = response.data?.itemsByType?.map { fragments ->
                fragments?.toPricing()
            } ?: emptyList()

            val ms = measureTimeMillis {
                priceDao.insertAll(items.mapNotNull {
                    Price(
                        id = it!!.id,
                        pricing = it,
                        updated = it.updated ?: ""
                    )
                })
            }

            //Timber.d("Updated ${items.count()} prices in $ms ms")

            ListenableWorker.Result.success()

        } catch (e: Exception) {
            e.printStackTrace()

            ListenableWorker.Result.failure()
        }
    }

     suspend fun populateQuests(): ListenableWorker.Result {
        return try {
            val questDao = tarkovRepo.getQuestDao()
            val response = apolloClient.query(QuestsQuery()).fetchPolicy(FetchPolicy.NetworkFirst).execute()
            val quests = response.data?.quests?.map { quest ->
                quest?.toQuest()
            } ?: emptyList()

            val ms = measureTimeMillis {
                questDao.insertAll(quests.filterNotNull())
            }

            //Timber.d("Inserted ${quests.count()} quests in $ms ms")

            return ListenableWorker.Result.success()
        } catch (e: Exception) {
            e.printStackTrace()

            ListenableWorker.Result.failure()
        }
    }

     suspend fun populateCrafts(): ListenableWorker.Result {
        return try {
            val craftDao = tarkovRepo.getCraftDao()
            val response = apolloClient.query(CraftsQuery()).fetchPolicy(FetchPolicy.NetworkFirst).execute()
            val crafts = response.data?.crafts?.map { craft ->
                craft?.toCraft()
            } ?: emptyList()

            return if (crafts.isNotEmpty()) {
                val ms = measureTimeMillis {
                    craftDao.insertAll(crafts.filterNotNull())
                }

                //Timber.d("Inserted ${crafts.count()} crafts in $ms ms")

                ListenableWorker.Result.success()
            } else ListenableWorker.Result.failure()
        }  catch (e: Exception) {
            e.printStackTrace()

            ListenableWorker.Result.failure()
        }
    }

     suspend fun populateBarters(): ListenableWorker.Result {
        return try {
            val barterDao = tarkovRepo.getBarterDao()
            val response = apolloClient.query(BartersQuery()).fetchPolicy(FetchPolicy.NetworkFirst).execute()
            val barters = response.data?.barters?.map { barter ->
                barter?.toBarter()
            } ?: emptyList()

            return if (barters.isNotEmpty()) {
                //Timber.d("NUKING BARTER TABLE")
                barterDao.nukeTable()

                val ms = measureTimeMillis {
                    barterDao.insertAll(barters.filterNotNull())
                }

                //Timber.d("Inserted ${barters.count()} barters in $ms ms")

                ListenableWorker.Result.success()
            } else ListenableWorker.Result.failure()
        }  catch (e: Exception) {
            e.printStackTrace()

            ListenableWorker.Result.failure()
        }
    }
}
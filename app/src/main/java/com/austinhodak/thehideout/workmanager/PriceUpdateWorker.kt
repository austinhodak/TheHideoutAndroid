package com.austinhodak.thehideout.workmanager

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.apollographql.apollo3.ApolloClient
import com.austinhodak.tarkovapi.BartersQuery
import com.austinhodak.tarkovapi.CraftsQuery
import com.austinhodak.tarkovapi.ItemsByTypeQuery
import com.austinhodak.tarkovapi.QuestsQuery
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.type.ItemType
import com.austinhodak.tarkovapi.utils.toBarter
import com.austinhodak.tarkovapi.utils.toCraft
import com.austinhodak.tarkovapi.utils.toPricing
import com.austinhodak.tarkovapi.utils.toQuest
import com.austinhodak.thehideout.widgets.SinglePriceWidget
import dagger.hilt.android.internal.Contexts.getApplication
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import javax.inject.Inject


class PriceUpdateWorker constructor(
    val tarkovRepo: TarkovRepo,
    val apolloClient: ApolloClient,
    val appContext: Context,
    workerParameters: WorkerParameters
): CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        Timber.d("RUNNING!")
        val preferences = appContext.getSharedPreferences("tarkov", Context.MODE_PRIVATE)

        val test = coroutineScope {
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

        preferences.edit().putLong("lastPriceUpdate", System.currentTimeMillis()).apply()

        val widgetIntent = Intent(appContext, SinglePriceWidget::class.java)
        widgetIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids: IntArray = AppWidgetManager.getInstance(getApplication(appContext))
            .getAppWidgetIds(ComponentName(getApplication(appContext), SinglePriceWidget::class.java))
        widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)

        appContext.sendBroadcast(widgetIntent)

        return if (test.all { it == Result.success() }) Result.success() else Result.failure()
    }

    private suspend fun updatePricing(): Result {

        return try {
            val itemDao = tarkovRepo.getItemDao()
            val response = apolloClient.query(ItemsByTypeQuery(ItemType.any))
            val items = response.data?.itemsByType?.map { fragments ->
                fragments?.toPricing()
            } ?: emptyList()

            //val itemsChunked = items.chunked(900)

            for (item in items) {
                Timber.d("UPDATE PRICING | ${item?.shortName} | ${item?.id} | ${items.indexOf(item)}/${items.count()}")
                if (item != null)
                    itemDao.updateAllPricing(item.id, item)
            }

            Result.success()

        } catch (e: Exception) {
            e.printStackTrace()

            Result.failure()
        }
    }

    private suspend fun populateQuests(): Result {
        return try {
            val questDao = tarkovRepo.getQuestDao()
            val response = apolloClient.query(QuestsQuery())
            val quests = response.data?.quests?.map { quest ->
                quest?.toQuest(null)
            } ?: emptyList()

            for (quest in quests) {
                if (quest != null) {
                    Timber.d("Updating Quest ${quest.id}")
                    questDao.insert(quest)
                }
            }

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()

            Result.failure()
        }
    }

    private suspend fun populateCrafts(): Result {
        return try {
            val craftDao = tarkovRepo.getCraftDao()
            val response = apolloClient.query(CraftsQuery())
            val crafts = response.data?.crafts?.map { craft ->
                craft?.toCraft()
            } ?: emptyList()

            return if (crafts.isNotEmpty()) {
                Timber.d("NUKING CRAFT TABLE")
                craftDao.nukeTable()

                for (craft in crafts) {
                    if (craft != null) {
                        Timber.d("Updating Craft")
                        craftDao.insert(craft)
                    }
                }

                Result.success()
            } else Result.failure()
        }  catch (e: Exception) {
            e.printStackTrace()

            Result.failure()
        }
    }

    private suspend fun populateBarters(): Result {
        return try {
            val barterDao = tarkovRepo.getBarterDao()
            val response = apolloClient.query(BartersQuery())
            val barters = response.data?.barters?.map { barter ->
                barter?.toBarter()
            } ?: emptyList()

            return if (barters.isNotEmpty()) {
                Timber.d("NUKING BARTER TABLE")
                barterDao.nukeTable()

                for (barter in barters) {
                    if (barter != null) {
                        Timber.d("Updating Barter")
                        barterDao.insert(barter)
                    }
                }
                Result.success()
            } else Result.failure()
        }  catch (e: Exception) {
            e.printStackTrace()

            Result.failure()
        }
    }

    class Factory @Inject constructor(
        val tarkovRepo: TarkovRepo,
        val apolloClient: ApolloClient,
    ): ChildWorkerFactory {
        override fun create(appContext: Context, params: WorkerParameters): CoroutineWorker {
            return PriceUpdateWorker(tarkovRepo, apolloClient, appContext, params)
        }
    }
}

interface ChildWorkerFactory {
    fun create(appContext: Context, params: WorkerParameters): CoroutineWorker
}
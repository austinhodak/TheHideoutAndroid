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
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.internal.Contexts.getApplication
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import javax.inject.Inject
import kotlin.system.measureTimeMillis


class PriceUpdateWorker @AssistedInject constructor(
    val tarkovRepo: TarkovRepo,
    val apolloClient: ApolloClient,
    @Assisted private val appContext: Context,
    @Assisted private val workerParameters: WorkerParameters,
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
                quest?.toQuest()
            } ?: emptyList()

            val ms = measureTimeMillis {
                questDao.insertAll(quests.filterNotNull())
            }

            Timber.d("QUESTS | ${quests.count()} | $ms")

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

                val ms = measureTimeMillis {
                    craftDao.insertAll(crafts.filterNotNull())
                }

                Timber.d("Inserted ${crafts.count()} crafts in $ms ms")

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

                val ms = measureTimeMillis {
                    barterDao.insertAll(barters.filterNotNull())
                }

                Timber.d("Inserted ${barters.count()} barters in $ms ms")

                Result.success()
            } else Result.failure()
        }  catch (e: Exception) {
            e.printStackTrace()

            Result.failure()
        }
    }

    /**
     * class annotate with @AssistedFactory will available in the dependency graph, you don't need
     * additional binding from [HelloWorldWorker_Factory_Impl] to [Factory].
     */
    @AssistedFactory
    interface Factory {
        fun create(appContext: Context, params: WorkerParameters): PriceUpdateWorker
    }
}
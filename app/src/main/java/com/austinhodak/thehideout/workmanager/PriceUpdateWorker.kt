package com.austinhodak.thehideout.workmanager

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.apollographql.apollo3.ApolloClient
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.Updaters
import com.austinhodak.thehideout.widgets.SinglePriceWidget
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.internal.Contexts.getApplication
import timber.log.Timber


class PriceUpdateWorker @AssistedInject constructor(
    val tarkovRepo: TarkovRepo,
    val apolloClient: ApolloClient,
    @Assisted private val appContext: Context,
    @Assisted private val workerParameters: WorkerParameters
): CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        Timber.d("RUNNING!")
        val preferences = appContext.getSharedPreferences("tarkov", Context.MODE_PRIVATE)

        val updateAll = Updaters(tarkovRepo, apolloClient).updateAll()

        preferences.edit().putLong("lastPriceUpdate", System.currentTimeMillis()).apply()

        val widgetIntent = Intent(appContext, SinglePriceWidget::class.java)
        widgetIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids: IntArray = AppWidgetManager.getInstance(getApplication(appContext))
            .getAppWidgetIds(ComponentName(getApplication(appContext), SinglePriceWidget::class.java))
        widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)

        appContext.sendBroadcast(widgetIntent)

        return if (updateAll.all { it == Result.success() }) {
            Result.success()
        } else {
            Result.failure()
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(appContext: Context, params: WorkerParameters): PriceUpdateWorker
    }
}
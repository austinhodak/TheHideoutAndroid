package com.austinhodak.thehideout.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.tracing.traceAsync
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.austinhodak.thehideout.apollo.TarkovApiRepository
import com.austinhodak.thehideout.base.util.Dispatcher
import com.austinhodak.thehideout.base.util.NiaDispatchers.IO
import com.austinhodak.thehideout.realm.RealmRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

// All sync work needs an internet connectionS
val SyncConstraints
    get() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    tarkovApiRepository: TarkovApiRepository,
    realmRepository: RealmRepository,
    val sync: Sync,
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(ioDispatcher) {
        val syncType = SyncType.fromArray(inputData.getStringArray("SYNC_TYPE") ?: arrayOf("NONE"))

        var progress = 0
        setProgress(workDataOf("PROGRESS" to progress))
        var totalTasks = 0

        traceAsync("SyncWorker", 0) {
            val syncTasks = mutableListOf<suspend () -> Boolean>()
            if (syncType has SyncType.ITEMS) {
                totalTasks++
                syncTasks.add { runAndCheck { sync.syncItems() } }
            }
            if (syncType has SyncType.TASKS) {
                totalTasks++
                syncTasks.add { runAndCheck { sync.syncTasks() } }
            }
            if (syncType has SyncType.OTHER) {
                totalTasks++
                syncTasks.add { runAndCheck { sync.syncOther() } }
            }

            if (syncType has SyncType.TRADERS) {
                totalTasks++
                syncTasks.add { runAndCheck { sync.syncTraders() } }
            }

            if (syncType has SyncType.AMMO) {
                totalTasks ++
                syncTasks.add { runAndCheck { sync.syncAmmo() } }
            }

            if (syncType has SyncType.MAPS) {
                totalTasks++
                syncTasks.add { runAndCheck { sync.syncMaps() } }
            }

            if (syncType has SyncType.BARTERS) {
                totalTasks++
                syncTasks.add { runAndCheck { sync.syncBarters() } }
            }

            if (syncType has SyncType.CRAFTS) {
                totalTasks++
                syncTasks.add { runAndCheck { sync.syncCrafts() } }
            }

            if (syncType has SyncType.BOSSES) {
                totalTasks++
                syncTasks.add { runAndCheck { sync.syncMobInfo() } }
            }

            if (syncType has SyncType.QUESTITEMS) {
                totalTasks++
                syncTasks.add { runAndCheck { sync.syncQuestItems() } }
            }

            if (syncType has SyncType.HIDEOUT) {
                totalTasks++
                syncTasks.add { runAndCheck { sync.syncHideout() } }
            }

            val syncedSuccessfully = syncTasks.map { async {
                val successful = it()
                if (successful) {
                    progress += (100 / totalTasks)
                    setProgress(workDataOf("PROGRESS" to progress))
                }
                successful
            } }.awaitAll()

            if (syncedSuccessfully.all { it }) Result.success()
            else Result.retry()
        }
    }

    private suspend fun runAndCheck(block: suspend () -> Unit): Boolean {
        return try {
            block()
            true
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        fun sync(vararg syncType: SyncType) = OneTimeWorkRequestBuilder<SyncWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(SyncConstraints)
            .setInputData(workDataOf("SYNC_TYPE" to SyncType.toArray(*syncType)))
            .build()
    }
}

infix fun List<SyncType>.has(other: SyncType) = this.contains(other)

enum class SyncType (val value: String) {
    ITEMS ("items"),
    TASKS ("tasks"),
    OTHER ("other"),
    TRADERS ("traders"),
    AMMO ("ammo"),
    CRAFTS ("crafts"),
    BARTERS ("barter"),
    BOSSES ("bosses"),
    HIDEOUT ("hideout"),
    MAPS ("maps"),
    QUESTITEMS ("questitems"),
    NONE ("none");

    companion object {
        fun toArray(vararg types: SyncType): Array<String> {
            return types.map { it.name }.toTypedArray()
        }

        fun fromArray(array: Array<String>): List<SyncType> {
            return array.map { SyncType.valueOf(it) }
        }
    }
}
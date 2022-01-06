package com.austinhodak.thehideout.workmanager

import android.content.Context
import androidx.work.*
import javax.inject.Inject
import javax.inject.Provider

class PriceUpdateFactory @Inject constructor(
    private val helloWorldWorkerFactory: PriceUpdateWorker.Factory,
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? {
        return when (workerClassName) {
            PriceUpdateWorker::class.java.name ->
                helloWorldWorkerFactory.create(appContext, workerParameters)
            else -> null
        }
    }
}
package com.austinhodak.tarkovapi.workmanager

import androidx.work.CoroutineWorker
import androidx.work.Worker
import com.austinhodak.tarkovapi.ChildWorkerFactory
import com.austinhodak.tarkovapi.PriceUpdateWorker
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class WorkerKey(val value: KClass<out CoroutineWorker>)

@Module
@InstallIn(SingletonComponent::class)
abstract class WorkerModule {

    @Binds
    @IntoMap
    @WorkerKey(PriceUpdateWorker::class)
    internal abstract fun bindMyWorker(worker: PriceUpdateWorker.Factory): ChildWorkerFactory
}
package com.austinhodak.thehideout.workmanager

import androidx.work.CoroutineWorker
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import kotlin.reflect.KClass
import com.austinhodak.thehideout.workmanager.ChildWorkerFactory
import com.austinhodak.thehideout.workmanager.PriceUpdateWorker

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
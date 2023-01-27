package com.austinhodak.thehideout.di

import com.austinhodak.thehideout.data.sync.SyncStatusMonitor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface SyncModule {
    @Binds
    fun bindsSyncStatusMonitor(
        syncStatusMonitor: SyncStatusMonitor
    ): SyncStatusMonitor
}

package com.austinhodak.thehideout.di

import android.content.Context
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo3.cache.normalized.api.NormalizedCacheFactory
import com.apollographql.apollo3.cache.normalized.sql.SqlNormalizedCacheFactory
import com.austinhodak.thehideout.data.sync.SyncStatusMonitor
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object ApolloModule {
    @Singleton
    @Provides
    fun providesApolloClient(
        okHttpClient: OkHttpClient
    ): ApolloClient = ApolloClient.Builder()
        .serverUrl("https://api.tarkov.dev/graphql")
        //.normalizedCache(normalizedCacheFactory)
        //.okHttpClient(okHttpClient)
        .build()

    @Singleton
    @Provides
    fun providesApolloCache(
        @ApplicationContext appContext: Context
    ): NormalizedCacheFactory {
        val inMemoryCacheFactory = MemoryCacheFactory(maxSizeBytes = 50 * 1024 * 1024)
        val sqlCacheFactory = SqlNormalizedCacheFactory(appContext, "apollo.db")

        return inMemoryCacheFactory.chain(sqlCacheFactory)
    }
}
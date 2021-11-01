package com.austinhodak.tarkovapi.di

import android.content.Context
import androidx.room.Room
import com.apollographql.apollo3.ApolloClient
import com.austinhodak.tarkovapi.room.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Module {

    @Provides
    fun provideApolloClient(): ApolloClient {
        return ApolloClient(
            serverUrl = "https://tarkov-tools.com/graphql"
        )
    }

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext appContext: Context,
        callback: AppDatabase.Callback
    ) = Room.databaseBuilder(appContext, AppDatabase::class.java, "hideout-database")
        .createFromAsset("hideout_database_43.db")
        .fallbackToDestructiveMigration()
        .addCallback(callback)
        .build()

    @Provides
    fun providesAmmoDao(database: AppDatabase) = database.AmmoDao()

    @Provides
    fun providesItemDao(database: AppDatabase) = database.ItemDao()

    @Provides
    fun providesWeaponDao(database: AppDatabase) = database.WeaponDao()

    @Provides
    fun providesQuestDao(database: AppDatabase) = database.QuestDao()

    @Provides
    fun providesBarterDao(database: AppDatabase) = database.BarterDao()

    @Provides
    fun providesCraftDao(database: AppDatabase) = database.CraftDao()

    @Provides
    fun providesModDao(database: AppDatabase) = database.ModDao()

    @ApplicationScope
    @Provides
    @Singleton
    fun providesApplicationScope() = CoroutineScope(SupervisorJob())
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope
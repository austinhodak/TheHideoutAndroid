package com.austinhodak.tarkovapi.di

import com.austinhodak.tarkovapi.networking.TarkovApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepoModule {

    @Singleton
    @Provides
    fun provideWebService() = TarkovApi()

}
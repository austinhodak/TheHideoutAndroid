package com.austinhodak.tarkovapi.di

import com.austinhodak.tarkovapi.repository.TarkovRepository
import com.austinhodak.tarkovapi.repository.TarkovRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
abstract class ViewModelModule {

    @Binds
    @ViewModelScoped
    abstract fun bindRepository(repo: TarkovRepositoryImpl): TarkovRepository

}
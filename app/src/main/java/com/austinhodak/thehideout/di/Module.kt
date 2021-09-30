package com.austinhodak.thehideout.di

import android.content.Context
import com.austinhodak.thehideout.billing.BillingClientWrapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object Module {

    @Provides
    fun provideBillingWrapper(
        @ApplicationContext context: Context
    ): BillingClientWrapper {
        return BillingClientWrapper(context)
    }
}
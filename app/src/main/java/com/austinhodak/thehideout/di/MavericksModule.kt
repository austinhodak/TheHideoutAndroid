package com.austinhodak.thehideout.di

import com.airbnb.mvrx.hilt.AssistedViewModelFactory
import com.airbnb.mvrx.hilt.MavericksViewModelComponent
import com.airbnb.mvrx.hilt.ViewModelKey
import com.austinhodak.thehideout.features.premium.viewmodels.PremiumViewModel
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.multibindings.IntoMap

@Module
@InstallIn(MavericksViewModelComponent::class)
interface MavericksModule {

    @Binds
    @IntoMap
    @ViewModelKey(PremiumViewModel::class)
    fun bindsPremiumViewModel(
        factory: PremiumViewModel.Factory
    ): AssistedViewModelFactory<*, *>

}
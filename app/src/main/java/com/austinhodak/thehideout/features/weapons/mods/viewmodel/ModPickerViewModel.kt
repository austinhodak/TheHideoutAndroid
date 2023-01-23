package com.austinhodak.thehideout.features.weapons.mods.viewmodel

import com.austinhodak.tarkovapi.repository.ModsRepo
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.thehideout.SearchViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ModPickerViewModel @Inject constructor(
        private val tarkovRepo: TarkovRepo,
        private val modsRepo: ModsRepo
) : SearchViewModel() {

}
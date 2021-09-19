package com.austinhodak.thehideout.gear.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.models.Ammo
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.thehideout.SearchViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GearViewModel @Inject constructor(
    private val repository: TarkovRepo
) : SearchViewModel() {

    private val _gearDetails = MutableLiveData<Item?>(null)
    val gearDetails = _gearDetails

    fun getGear(id: String) {
        viewModelScope.launch {
            repository.getItemByID(id).collect {
                _gearDetails.value = it
            }
        }
    }

    private val _selectedAmmo = MutableLiveData<Ammo?>(null)
    val selectedAmmo = _selectedAmmo

    fun selectAmmo(ammo: Ammo) {
        _selectedAmmo.value = ammo
    }

    var sortBy = MutableLiveData(0)

    fun setSort(int: Int) {
        sortBy.value = int
    }
}
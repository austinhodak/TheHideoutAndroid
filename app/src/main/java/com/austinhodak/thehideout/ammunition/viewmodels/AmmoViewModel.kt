package com.austinhodak.thehideout.ammunition.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.models.Ammo
import com.austinhodak.tarkovapi.room.models.Item
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AmmoViewModel @Inject constructor(
    private val repository: TarkovRepo
) : ViewModel() {

    private val _sort = MutableLiveData(0)
    val sort = _sort

    fun setSort(index: Int) {
        _sort.value = index
    }

    private val _ammoDetails = MutableLiveData<Ammo?>(null)
    val ammoDetails = _ammoDetails

    fun getAmmo(id: String) {
        viewModelScope.launch {
            repository.getAmmoByID(id).catch { e ->

            }.collect {
                _ammoDetails.value = it
            }
        }
    }

    private val _selectedArmor = MutableLiveData<Item?>(null)
    val selectedArmor = _selectedArmor

    fun selectArmor(item: Item) {
        _selectedArmor.value = item
    }

}
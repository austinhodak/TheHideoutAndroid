package com.austinhodak.thehideout.weapons.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.models.Weapon
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeaponDetailViewModel @Inject constructor(
    private val tarkovRepo: TarkovRepo
) : ViewModel() {


    private val _weaponDetails = MutableLiveData<Weapon?>(null)
    val weaponDetails = _weaponDetails

    fun getWeapon(id: String) {
        viewModelScope.launch {
            tarkovRepo.getWeaponByID(id).catch { e ->

            }.collect {
                _weaponDetails.value = it
            }
        }
    }
}
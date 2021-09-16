package com.austinhodak.thehideout.weapons.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.room.models.Weapon
import com.austinhodak.thehideout.WeaponBuild
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeaponDetailViewModel @Inject constructor(
    private val tarkovRepo: TarkovRepo
) : ViewModel() {

    private val _weaponDetails = MutableLiveData<Weapon?>(null)
    val weaponDetails = _weaponDetails

    private val _weaponMods = MutableLiveData<MutableList<Item>?>(null)
    val weaponMods = _weaponMods

    fun getWeapon(id: String) {
        viewModelScope.launch {
            tarkovRepo.getWeaponByID(id).collect {
                /*if (weaponDetails.value == null)
                it.getAllMods()?.let { ids ->
                    //test(ids)
                    *//*tarkovRepo.getItemByID(ids).collect { mods ->
                        mods.distinct().forEach {
                            //Timber.d("${it.id} | ${it.Name}")


                        }
                    }*//*
                }*/
                _weaponDetails.value = it
            }
        }
    }

    private val _weaponBuild = MutableLiveData<WeaponBuild>(null)
    val weaponBuild = _weaponBuild

    fun updateBuild(build: WeaponBuild) {
        weaponBuild.postValue(build)
    }

    /*fun test(ids: List<String>?) {
        //Timber.d(ids.toString())
        if (ids.isNullOrEmpty()) return
        Timber.d("1 | $ids")
        viewModelScope.launch {
            val mods = tarkovRepo.getItemByID(ids)
            mods.distinctBy { it.id }.forEach { item ->
                Timber.d(item.Name)
                _weaponMods.value?.add(item)
                if (item.Slots.isNullOrEmpty()) return@forEach
                test(item.getAllMods())
            }
            *//*tarkovRepo.getItemByID(ids).collect { mods ->
                Timber.d("2 | ${mods.size}")
                mods.distinctBy { it.id }.forEach { item ->
                    //Timber.d("${it.id} | ${it.Name}")
                    Timber.d("3 | ${item.ShortName}")
                    if (item.Slots.isNullOrEmpty()) return@forEach
                    //test(item.getAllMods())
                }
            }*//*
        }
    }*/
}
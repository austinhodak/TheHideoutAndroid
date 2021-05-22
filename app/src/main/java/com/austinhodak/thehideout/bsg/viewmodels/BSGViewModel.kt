package com.austinhodak.thehideout.bsg.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.austinhodak.thehideout.bsg.BSGRepository
import com.austinhodak.thehideout.bsg.models.ammo.BsgAmmo
import com.austinhodak.thehideout.bsg.models.mod.BsgMod
import com.austinhodak.thehideout.bsg.models.weapon.BsgWeapon
import com.austinhodak.thehideout.bsg.models.weapon.WeaponClass
import kotlinx.coroutines.launch

class BSGViewModel (application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext

    private val _weaponClasses = BSGRepository.weaponClasses
    fun weaponClasses(): MutableSet<WeaponClass> = _weaponClasses

    private val _allData = MutableLiveData<List<Any>>()
    fun allData(): LiveData<List<Any>> = _allData

    init {
        viewModelScope.launch {
            _allData.value = BSGRepository.processRawData(context).apply {
                _allWeapons.value = this.filterIsInstance(BsgWeapon::class.java)
                _allAmmo.value = this.filterIsInstance(BsgAmmo::class.java)
                _allMods.value = this.filterIsInstance(BsgMod::class.java)
            }
        }
    }

    private val _allWeapons = MutableLiveData<List<BsgWeapon>>()
    fun getWeapons(): LiveData<List<BsgWeapon>> = _allWeapons

    private val _allAmmo = MutableLiveData<List<BsgAmmo>>()
    fun getAmmo(): LiveData<List<BsgAmmo>> = _allAmmo

    private val _allMods = MutableLiveData<List<BsgMod>>()
    fun getMods(): LiveData<List<BsgMod>> = _allMods
}
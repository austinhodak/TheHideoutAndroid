package com.austinhodak.thehideout.weapons.builder.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.austinhodak.tarkovapi.repository.ModsRepo
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.models.Mod
import com.austinhodak.thehideout.WeaponBuild
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class WeaponBuilderViewModel @Inject constructor(
        private val tarkovRepo: TarkovRepo,
        private val modsRepo: ModsRepo
) : ViewModel() {

    var buildState by mutableStateOf<WeaponBuild?>(WeaponBuild())

    fun setParentWeapon(weaponID: String) {
        val b = buildState?.copy() ?: WeaponBuild()

        viewModelScope.launch {
            tarkovRepo.getWeaponByID(weaponID).first {
                b.parentWeapon = it
                buildState = b

                it.defAmmo?.let {
                    tarkovRepo.getAmmoByID(it).first {
                        b.ammo = it
                        buildState = b
                        true
                    }
                }

                true
            }
        }
    }

    fun resetBuild() {
        val b = buildState?.copy()

        buildState = WeaponBuild()
        b?.parentWeapon?.id?.let { setParentWeapon(it) }
    }

    fun removeMod(mod: Mod?, slotID: String?, parentID: String?) {
        val b = buildState?.copy() ?: WeaponBuild()
        slotID?.let {
            b.mods = b.mods!! - it
        }
        buildState = b
    }

    fun updateMod(modID: String?, slotID: String?, parentID: String?) {
        if (modID == null || slotID == null || parentID == null) return
        viewModelScope.launch {
            modsRepo.getModByID(modID).first {
                updateMod(it, slotID, parentID)
                true
            }
        }
    }

    fun updateMod(mod: Mod, slotID: String, parentID: String) {
        val b = buildState?.copy() ?: WeaponBuild()
        if (parentID == b.parentWeapon?.id) {
            b.mods = b.mods!! + mapOf(
                    Pair(
                            slotID,
                            WeaponBuild.BuildMod(
                                    parent = parentID,
                                    mod = mod
                            )
                    )
            )
            buildState = b
        } else {
            b.mods = b.mods!! + mapOf(
                    Pair(
                            slotID,
                            WeaponBuild.BuildMod(
                                    parent = parentID,
                                    mod = mod
                            )
                    )
            )
            buildState = b
        }
    }
}
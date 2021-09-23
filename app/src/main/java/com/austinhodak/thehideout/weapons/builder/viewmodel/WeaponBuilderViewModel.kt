package com.austinhodak.thehideout.weapons.builder.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.austinhodak.tarkovapi.repository.ModsRepo
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.models.Mod
import com.austinhodak.tarkovapi.room.models.Weapon
import com.austinhodak.thehideout.WeaponBuild
import com.austinhodak.thehideout.firebase.WeaponBuildFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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

    var savedBuild: WeaponBuildFirestore? = null

    fun loadBuild(build: WeaponBuildFirestore) {
        //if (savedBuild != null) return
        savedBuild = build
        val b = buildState?.copy() ?: WeaponBuild()
        viewModelScope.launch {
            build.weapon?.id?.let {
                setParentWeapon(it)
            }
            build.ammo?.let {
                setAmmo(it)
            }
            build.mods?.forEach { (slotID, modID) ->
                updateMod(modID, slotID, null)
            }
            Timber.d(build.mods.toString())
        }

        buildState = b
        buildState!!.name = build.name
        buildState!!.uid = build.uid
    }

    fun setAmmo(ammoID: String) {
        val b = buildState?.copy() ?: WeaponBuild()
        viewModelScope.launch {
            tarkovRepo.getAmmoByID(ammoID).first {
                b.ammo = it
                buildState = b
                true
            }
        }
    }

    fun setParentWeapon(weaponID: String) {
        val b = buildState?.copy() ?: WeaponBuild()

        viewModelScope.launch {
            tarkovRepo.getWeaponByID(weaponID).first {
                b.parentWeapon = it
                it.Slots?.forEach {
                    val s = Pair (it, null)
                    //b.slots = b.slots?.plus(s)
                }
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

    fun setParentWeapon(weapon: Weapon) {
        val b = buildState?.copy() ?: WeaponBuild()

        viewModelScope.launch {
            b.parentWeapon = weapon
            buildState = b

            weapon.defAmmo?.let {
                tarkovRepo.getAmmoByID(it).first {
                    b.ammo = it
                    buildState = b
                    true
                }
            }
        }
    }

    fun resetBuild() {
        val b = buildState?.copy()

        buildState = WeaponBuild()
        b?.parentWeapon?.id?.let { setParentWeapon(it) }
    }

    fun removeMod(mod: Mod?, slotID: String?, parentID: String?, slot: Weapon.Slot) {
        val b = buildState?.copy() ?: WeaponBuild()

        b.mods?.get(slotID)?.mod?.Slots?.let {
            slotID?.let {
                b.mods = b.mods!! - it
            }
            buildState = b
            it.forEach { slot ->
                Timber.d(slot._id)
                removeMod(null, slot._id, null, slot)
            }
        }
    }

    fun updateMod(mod: Mod, slot: Weapon.Slot) {
        val b = buildState?.copy() ?: WeaponBuild()
        val mMod = b.parentWeapon?.Slots?.find { it == slot }?.apply {
            this.mod = mod
        }
        Timber.d(mMod.toString())
        buildState = b
    }

    fun updateMod(modID: String?, slotID: String?, parentID: String?) {
        if (modID == null || slotID == null) return
        viewModelScope.launch {
            modsRepo.getModByID(modID).first {
                updateMod(it, slotID)
                true
            }
        }
    }

    fun updateMod(mod: Mod, slotID: String) {
        val b = buildState?.copy() ?: WeaponBuild()
        /*if (parentID == b.parentWeapon?.id) {
            b.mods = b.mods!! + mapOf(
                    Pair(
                            slotID,
                            WeaponBuild.BuildMod(
                                    //parent = parentID,
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
                                    //parent = parentID,
                                    mod = mod
                            )
                    )
            )
            buildState = b
        }*/

        b.mods = b.mods!! + mapOf(
                Pair(
                        slotID,
                        WeaponBuild.BuildMod(
                                //parent = parentID,
                                mod = mod
                        )
                )
        )
        buildState = b
    }

    fun saveBuild(done: (Boolean) -> Unit) {
        if (savedBuild == null) {
            buildState?.toFirestore()?.let { Firebase.firestore.collection("loadouts").add(it) }
        } else if (savedBuild != null) {
            buildState?.toFirestore()?.let {
                savedBuild!!.id?.let { id ->
                    buildState?.toFirestore()?.let { save ->
                        Firebase.firestore.collection("loadouts").document(id).set(save
                        ).addOnFailureListener {
                            done(false)
                        }.addOnSuccessListener {
                            done(true)
                        }
                    }
                }
            }
        }
    }
}
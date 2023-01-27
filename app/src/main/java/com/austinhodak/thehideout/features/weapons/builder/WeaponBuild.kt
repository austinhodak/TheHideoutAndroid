package com.austinhodak.thehideout.features.weapons.builder

import com.austinhodak.tarkovapi.room.models.Ammo
import com.austinhodak.tarkovapi.room.models.Mod
import com.austinhodak.tarkovapi.room.models.Weapon
import com.austinhodak.thehideout.utils.uid
import com.google.firebase.Timestamp
import kotlin.math.roundToInt

data class WeaponBuild (
    var id: String? = null,
    var parentWeapon: Weapon? = null,
    var name: String? = null,
    var uid: String? = null,
    var ammo: Ammo? = null,
    // STRING = SLOT ID
    var mods: Map<String, BuildMod>? = HashMap()
) {

    data class BuildMod (
        var parent: String? = null,
        var mod: Mod? = null,
        var id: String? = null
    )

    fun toFirestore(): Map<String, Any?> {
        return mapOf(
            "ammo" to ammo?.id,
            "weapon" to hashMapOf(
                "id" to parentWeapon?.id,
                "name" to parentWeapon?.Name,
                "shortName" to parentWeapon?.ShortName
            ),
            "uid" to uid(),
            "name" to "${parentWeapon?.ShortName} Build",
            "id" to id,
            "mods" to mods?.mapValues { it.value.mod?.id },
            "stats" to hashMapOf(
                "ergonomics" to totalErgo(),
                "verticalRecoil" to totalVerticalRecoil(),
                "horizontalRecoil" to totalHorizontalRecoil(),
                "velocity" to totalVelocity(),
                "weight" to totalWeight(),
                "costRoubles" to totalCostFleaMarket()
            ),
            "timestamp" to Timestamp.now()
        )
    }

    fun allConflictingItems(): List<String>? {
        return mods?.flatMap { (_, value) ->
            value.mod?.ConflictingItems ?: emptyList()
        }
    }

    fun totalCostFleaMarket(): Int? {
        return mods?.map { it.value.mod?.pricing }?.sumOf {
            it?.getCheapestBuyRequirements()?.getPriceAsRoubles() ?: 0
        }?.plus(parentWeapon?.pricing?.getCheapestBuyRequirements()?.getPriceAsRoubles() ?: 0)
    }

    fun totalVelocity(): Int? {
        val velocity = mods?.map { it.value.mod }?.sumOf {
            (it?.Velocity) ?: 0.0
        } ?: 0.0
        val velocityPercent = velocity / 100.0
        val perc = (ammo?.ballistics?.initialSpeed?.toDouble()?.times(velocityPercent))
        val weaponVelocity = ammo?.ballistics?.initialSpeed?.toDouble()

        return when {
            velocity > 0.0 -> {
                weaponVelocity?.minus(perc ?: 0.0)?.roundToInt()
            }
            velocity < 0.0 -> {
                weaponVelocity?.plus(perc ?: 0.0)?.roundToInt()
            }
            else -> {
                weaponVelocity?.roundToInt()
            }
        }
    }

    fun totalErgo(): Int? {
        val modErgoSum = mods?.map { it.value.mod }?.sumOf {
            (it?.Ergonomics) ?: 0.0
        }
        return parentWeapon?.Ergonomics?.plus(modErgoSum ?: 0.0)?.roundToInt()
    }

    fun totalWeight(): Double? {
        val modWeightSum = mods?.map { it.value.mod }?.sumOf {
            (it?.Weight) ?: 0.0
        }
        return parentWeapon?.Weight?.plus(modWeightSum ?: 0.0)
    }

    // TODO FACTOR IN AMMUNITION
    fun totalVerticalRecoil(): Int? {
        val vertSum = mods?.map { it.value.mod }?.sumOf {
            (it?.Recoil) ?: 0.0
        } ?: 0.0
        val recoilPercent = vertSum / 100.0
        val perc = (parentWeapon?.RecoilForceUp?.toDouble()?.times(recoilPercent))
        val weaponRecoil = parentWeapon?.RecoilForceUp?.toDouble()

        return when {
            vertSum > 0.0 -> {
                weaponRecoil?.minus(perc ?: 0.0)?.roundToInt()
            }
            vertSum < 0.0 -> {
                weaponRecoil?.plus(perc ?: 0.0)?.roundToInt()
            }
            else -> {
                weaponRecoil?.roundToInt()
            }
        }
    }

    // TODO FACTOR IN AMMUNITION
    fun totalHorizontalRecoil(): Int? {
        val horzSum = mods?.map { it.value.mod }?.sumOf {
            (it?.Recoil) ?: 0.0
        } ?: 0.0
        val recoilPercent = horzSum / 100.0
        val perc = (parentWeapon?.RecoilForceBack?.toDouble()?.times(recoilPercent))
        val weaponRecoil = parentWeapon?.RecoilForceBack?.toDouble()

        return when {
            horzSum > 0.0 -> {
                weaponRecoil?.minus(perc ?: 0.0)?.roundToInt()
            }
            horzSum < 0.0 -> {
                weaponRecoil?.plus(perc ?: 0.0)?.roundToInt()
            }
            else -> {
                weaponRecoil?.roundToInt()
            }
        }
    }
}

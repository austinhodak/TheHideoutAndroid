package com.austinhodak.tarkovapi.room.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import org.json.JSONObject

@Entity(tableName = "weapons")
data class Weapon (
    @PrimaryKey var id: String,
    val AllowMisfire: Boolean? = null,
    //val AllowSpawnOnLocations: List<Any?>? = null,
    val BackgroundColor: String? = null,
    val BaseMalfunctionChance: Double? = null,
    val BoltAction: Boolean? = null,
    val BurstShotsCount: Double? = null,
    val Chambers: List<Chamber>? = null,
    //val ConflictingItems: List<Any?>? = null,
    val CreditsPrice: Double? = null,
    val Description: String? = null,
    val Durability: Double? = null,
    val Ergonomics: Double? = null,
    val ExamineExperience: Double? = null,
    val Foldable: Boolean? = null,
    val FoldedSlot: String? = null,
    //val Grids: List<Any?>? = null,
    val Height: Double? = null,
    val IronSightRange: Double? = null,
    val LootExperience: Double? = null,
    val Name: String? = null,
    val RagFairCommissionModifier: Double? = null,
    val Rarity: String? = null,
    val ReloadMode: String? = null,
    val RepairComplexity: Double? = null,
    val RepairCost: Double? = null,
    val RepairSpeed: Double? = null,
    val ShortName: String? = null,
    val Slots: List<Slot>? = null,
    val SpawnChance: Double? = null,
    val Velocity: Double? = null,
    val Weight: Double? = null,
    val Width: Double? = null,
    val ammoCaliber: String? = null,
    val bEffDist: Double? = null,
    val bFirerate: Double? = null,
    val bHearDist: Double? = null,
    val chamberAmmoCount: Double? = null,
    val defAmmo: String? = null,
    val defMagType: String? = null,
    val isBoltCatch: Boolean? = null,
    val isChamberLoad: Boolean? = null,
    val isFastReload: Boolean? = null,
    val shotgunDispersion: Double? = null,
    val weapClass: String? = null,
    val weapFireType: List<String>? = null,
    val weapUseType: String? = null,
    val pricing: Pricing? = null
) {
    data class Chamber(
        val _id: String? = null,
        val _mergeSlotWithChildren: Boolean? = null,
        val _name: String? = null,
        val _parent: String? = null,
        val _props: Props? = null,
        val _proto: String? = null,
        val _required: Boolean? = null
    ) {
        data class Props(
            val filters: List<Filter?>? = null
        ) {
            data class Filter(
                val Filter: List<String?>? = null
            )
        }
    }

    data class Slot(
        val _id: String? = null,
        val _mergeSlotWithChildren: Boolean? = null,
        val _name: String? = null,
        val _parent: String? = null,
        val _props: Props? = null,
        val _proto: String? = null,
        val _required: Boolean? = null
    ) {
        data class Props(
            val filters: List<Filter?>? = null
        ) {
            data class Filter(
                val AnimationIndex: Double? = null,
                val Filter: List<String?>? = null,
                val Shift: Double? = null
            )
        }
    }
}

fun JSONObject.toWeapon(id: String): Weapon {
    val weapon = Gson().fromJson(this.toString(), Weapon::class.java)
    weapon.id  = id
    return weapon
}
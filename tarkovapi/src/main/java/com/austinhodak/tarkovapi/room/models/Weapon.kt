package com.austinhodak.tarkovapi.room.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import org.json.JSONObject
import timber.log.Timber
import java.io.Serializable

@Entity(tableName = "weapons")
data class Weapon(
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
    val RecoilForceUp: Int? = null,
    val RecoilForceBack: Int? = null,
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
    ) : Serializable {
        data class Props(
            val filters: List<Filter?>? = null
        ) : Serializable {
            data class Filter(
                val AnimationIndex: Double? = null,
                val Filter: List<String?>? = null,
                val Shift: Double? = null
            ) : Serializable
        }

        fun getName(): String {
            return when (_name?.removePrefix("mod_")) {
                "magazine" -> "Magazine"
                "stock" -> "Stock"
                "foregrip" -> "Foregrip"
                "charge" -> "Charging Handle"
                "dust_cover" -> "Dust Cover"
                "optic" -> "Optic"
                "sight" -> "Sight"
                "mod_tactical_000",
                "mod_tactical_001",
                "mod_tactical_002",
                "mod_tactical_003",
                "mod_tactical_004",
                "mod_tactical_005",
                "tactical" -> "Tactical"
                "scope" -> "Scope"
                "mount_000",
                "mount_001",
                "mount_002",
                "mount_003",
                "mount_004",
                "mount_005",
                "mount_006",
                "mount" -> "Mount"
                "handguard" -> "Handguard"
                "barrel" -> "Barrel"
                "bipod" -> "Bipod"
                "other" -> "Other"
                "auxiliary" -> "Auxiliary"
                "gas_block" -> "Gas Block"
                "launcher" -> "Launcher"
                "muzzle" -> "Muzzle"
                "pistol_grip" -> "Pistol Grip"
                "reciever" -> "Receiver"
                "sight_rear" -> "Rear Sight"
                "mod_sight_front" -> "Front Sight"
                else -> _name ?: "Mod"
            }.uppercase()
        }

        fun getSubIds(): List<String>? {
            if (_props?.filters?.isNullOrEmpty() == true) return null
            return _props?.filters?.first()?.Filter?.filterNotNull()
        }
    }

    fun getAllMods(): List<String>? {
        if (Slots.isNullOrEmpty()) return null
        return Slots.flatMap {
            it._props?.filters?.first()?.Filter?.filterNotNull()!!
        }
    }

    fun getTarkovMarketImageURL(): String {
        val url = "https://cdn.tarkov-market.com/loadouts/images"
        val name = ShortName?.replace("\"", "")?.replace("(", "_")?.replace(")", "_")?.replace(" ", "_")?.lowercase()
        val getUrl = "$url/$name/default.jpg"
        Timber.d(getUrl)
        return getUrl
    }
}

fun JSONObject.toWeapon(id: String): Weapon {
    val weapon = Gson().fromJson(this.toString(), Weapon::class.java)
    weapon.id = id
    return weapon
}
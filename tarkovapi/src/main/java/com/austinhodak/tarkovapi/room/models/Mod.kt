package com.austinhodak.tarkovapi.room.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.austinhodak.tarkovapi.room.enums.ItemTypes
import com.austinhodak.tarkovapi.utils.asCurrency
import com.google.gson.Gson
import org.json.JSONObject
import java.io.Serializable

@Entity(tableName = "mods")
class Mod(
    @PrimaryKey var id: String,
    var itemType: ItemTypes? = ItemTypes.NONE,
    var parent: String? = null,
    val Name: String? = null,
    val ShortName: String? = null,
    val Description: String? = null,
    val Weight: Double? = null,
    val Width: Int? = null,
    val Height: Int? = null,
    val StackMaxSize: Int? = null,
    val Rarity: String? = null,
    val SpawnChance: Double? = null,
    val BackgroundColor: String? = null,
    val LootExperience: Int? = null,
    val ExamineExperience: Int? = null,
    val RepairCost: Int? = null,
    val Durability: Int? = null,
    val MaxDurability: Int? = null,
    val pricing: Pricing? = null,
    val Accuracy: Double? = null,
    val Recoil: Double? = null,
    val Loudness: Double? = null,
    val EffectiveDistance: Double? = null,
    val Ergonomics: Double? = null,
    val Velocity: Double? = null,
    val RaidModdable: Boolean? = null,
    val ToolModdable: Boolean? = null,
    val SightingRange: Double? = null,
    val muzzleModType: String? = null,
    val ExtraSizeLeft: Int? = null,
    val ExtraSizeRight: Int? = null,
    val ExtraSizeUp: Int? = null,
    val ExtraSizeDown: Int? = null,
    val MergesWithChildren: Boolean? = null,
    val ConflictingItems: List<String>? = null,
    val Slots: List<Weapon.Slot>? = null,
    // Magazines
    val Cartridges: List<Cartridge>? = null,
    // Scopes
    val sightModType: String? = null,
    val aimingSensitivity: Double? = null,
    val sighModesCount: Int? = null,
    val OpticCalibrationDistances: List<Int>? = null,
    val ScopesCount: Int? = null,
    //val ModesCount: List<Int>? = null,
    //val Zooms: List<List<Int>>? = null,
) : Serializable {
    data class Cartridge(
        val _id: String,
        val _max_count: Int,
        val _name: String,
        val _parent: String,
        val _props: Props,
        val _proto: String
    ) : Serializable {
        data class Props(
            val filters: List<Filter>
        ) : Serializable {
            data class Filter(
                val Filter: List<String>
            )
        }
    }

    fun getSlotModIDs(): List<String?>? {
        return Slots?.flatMap { it._props?.filters?.flatMap { it?.Filter!! }!! }
    }

    fun getTotalSlots(): Int {
        return Width?.times(Height ?: 1) ?: 1
    }

    fun getPrice(): Int {
        return pricing?.getPrice() ?: 0
    }

    fun getPricePerSlot(): Int {
        return getPrice() / getTotalSlots()
    }

    override fun toString(): String {
        return "R: $Recoil • E: $Ergonomics • A: $Accuracy% | ${getPrice().asCurrency()}"
    }
}

fun String.getModName(): String {
    return when (this) {
        "55818afb4bdc2dde698b456d" -> "Bipod"
        "55818af64bdc2d5b648b4570" -> "Foregrip"
        "55818b084bdc2d5b648b4571" -> "Flashlight"
        "55818b164bdc2ddc698b456c" -> "Combo Tactical Device"
        "550aa4dd4bdc2dc9348b4569" -> "Combo Muzzle Device"
        "550aa4cd4bdc2dd8348b456c" -> "Suppressor"
        "55818a6f4bdc2db9688b456b" -> "Charging Handle"
        "5448bc234bdc2d3c308b4569" -> "Magazine"
        "55818b224bdc2dde698b456f" -> "Mount"
        "55818a594bdc2db9688b456a" -> "Stock"
        "555ef6e44bdc2de9068b457e" -> "Barrel"
        "56ea9461d2720b67698b456f" -> "Gas Block"
        "55818a104bdc2db9688b4569" -> "Handguard"
        "55818a684bdc2ddd698b456d" -> "Grip"
        "55818a304bdc2db5418b457d" -> "Receiver"
        "55818add4bdc2d5b648b456f" -> "Assault Sight"
        "55818ad54bdc2ddc698b4569" -> "Reflex Sight"
        "55818acf4bdc2dde698b456b" -> "Compact Sight"
        "55818ac54bdc2d5b648b456e" -> "Iron Sight"
        "55818ae44bdc2dde698b456c" -> "Scope"
        "55818aeb4bdc2ddc698b456a" -> "Special Sight"
        "550aa4bf4bdc2dd6348b456b" -> "Muzzle Device"
        else -> ""
    }
}

fun getModName(item: Any): String {
    return when (item) {
        is Item -> {
            when (item.parent) {
                "55818afb4bdc2dde698b456d" -> "Bipod"
                "55818af64bdc2d5b648b4570" -> "Foregrip"
                "55818b084bdc2d5b648b4571" -> "Flashlight"
                "55818b164bdc2ddc698b456c" -> "Combo Tactical Device"
                "550aa4dd4bdc2dc9348b4569" -> "Combo Muzzle Device"
                "550aa4cd4bdc2dd8348b456c" -> "Suppressor"
                "55818a6f4bdc2db9688b456b" -> "Charging Handle"
                "5448bc234bdc2d3c308b4569" -> "Magazine"
                "55818b224bdc2dde698b456f" -> "Mount"
                "55818a594bdc2db9688b456a" -> "Stock"
                "555ef6e44bdc2de9068b457e" -> "Barrel"
                "56ea9461d2720b67698b456f" -> "Gas Block"
                "55818a104bdc2db9688b4569" -> "Handguard"
                "55818a684bdc2ddd698b456d" -> "Grip"
                "55818a304bdc2db5418b457d" -> "Receiver"
                "55818add4bdc2d5b648b456f" -> "Assault Sight"
                "55818ad54bdc2ddc698b4569" -> "Reflex Sight"
                "55818acf4bdc2dde698b456b" -> "Compact Sight"
                "55818ac54bdc2d5b648b456e" -> "Iron Sight"
                "55818ae44bdc2dde698b456c" -> "Scope"
                "55818aeb4bdc2ddc698b456a" -> "Special Sight"
                "550aa4bf4bdc2dd6348b456b" -> {
                    when (item.muzzleModType) {
                        "brake" -> "Muzzle Brake"
                        "conpensator" -> "Compensator"
                        "muzzleCombo" -> "Muzzle Device"
                        else -> "Muzzle"
                    }
                }
                else -> ""
            }
        }
        is Mod -> {
            when (item.parent) {
                "55818afb4bdc2dde698b456d" -> "Bipod"
                "55818af64bdc2d5b648b4570" -> "Foregrip"
                "55818b084bdc2d5b648b4571" -> "Flashlight"
                "55818b164bdc2ddc698b456c" -> "Combo Tactical Device"
                "550aa4dd4bdc2dc9348b4569" -> "Combo Muzzle Device"
                "550aa4cd4bdc2dd8348b456c" -> "Suppressor"
                "55818a6f4bdc2db9688b456b" -> "Charging Handle"
                "5448bc234bdc2d3c308b4569" -> "Magazine"
                "55818b224bdc2dde698b456f" -> "Mount"
                "55818a594bdc2db9688b456a" -> "Stock"
                "555ef6e44bdc2de9068b457e" -> "Barrel"
                "56ea9461d2720b67698b456f" -> "Gas Block"
                "55818a104bdc2db9688b4569" -> "Handguard"
                "55818a684bdc2ddd698b456d" -> "Grip"
                "55818a304bdc2db5418b457d" -> "Receiver"
                "55818add4bdc2d5b648b456f" -> "Assault Sight"
                "55818ad54bdc2ddc698b4569" -> "Reflex Sight"
                "55818acf4bdc2dde698b456b" -> "Compact Sight"
                "55818ac54bdc2d5b648b456e" -> "Iron Sight"
                "55818ae44bdc2dde698b456c" -> "Scope"
                "55818aeb4bdc2ddc698b456a" -> "Special Sight"
                "550aa4bf4bdc2dd6348b456b" -> {
                    when (item.muzzleModType) {
                        "brake" -> "Muzzle Brake"
                        "conpensator" -> "Compensator"
                        "muzzleCombo" -> "Muzzle Device"
                        else -> "Muzzle"
                    }
                }
                else -> ""
            }
        }
        else -> {
            ""
        }
    }
}

fun JSONObject.toMod(): Mod {
    val props = this.getJSONObject("_props")
    val mod = Gson().fromJson(props.toString(), Mod::class.java)
    mod.id = this.getString("_id")
    mod.parent = this.getString("_parent")
    return mod
}
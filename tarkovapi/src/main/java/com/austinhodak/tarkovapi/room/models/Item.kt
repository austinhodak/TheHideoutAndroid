package com.austinhodak.tarkovapi.room.models

import android.text.format.DateUtils
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.austinhodak.tarkovapi.room.enums.ItemTypes
import com.austinhodak.tarkovapi.utils.getItemType
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import org.json.JSONObject
import timber.log.Timber
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "items")
data class Item(
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
    //Armor
    val Slots: List<Weapon.Slot>? = null,
    val BlocksEarpiece: Boolean? = null,
    val BlocksEyewear: Boolean? = null,
    val BlocksHeadwear: Boolean? = null,
    val BlocksFaceCover: Boolean? = null,
    val BlocksArmorVest: Boolean? = null,
    val RigLayoutName: String? = null,
    val armorClass: String? = null,
    val speedPenaltyPercent: Double? = null,
    val mousePenalty: Double? = null,
    val weaponErgonomicPenalty: Double? = null,
    val armorZone: List<String>? = null,
    val Indestructibility: Double? = null,
    val headSegments: List<String>? = null,
    val FaceShieldComponent: Boolean? = null,
    val FaceShieldMask: String? = null,
    val HasHinge: Boolean? = null,
    val DeafStrength: String? = null,
    val BluntThroughput: Double? = null,
    val ArmorMaterial: String? = null,
    val BlindnessProtection: Double? = null,
    val Grids: List<Grid>? = null,
    //Mods
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
    //Meds
    val medUseTime: Double? = null,
    val medEffectType: String? = null,
    val MaxHpResource: Double? = null,
    val hpResourceRate: Double? = null,
    val StimulatorBuffs: String? = null,
    @SerializedName("effects_health_null")
    var effects_health: JSONObject? = null,
    @SerializedName("effects_damage_null")
    var effects_damage: JSONObject? = null,
) : Serializable {

    fun getCleanIcon(): String? = pricing?.iconLink

    fun getEnergy(): Int? {
        return effects_health?.optJSONObject("Energy")?.optInt("value")
    }

    fun getHydration(): Int? {
        return effects_health?.optJSONObject("Hydration")?.optInt("value")
    }

    fun cArmorClass(): Int {
        return armorClass?.toIntOrNull() ?: 0
    }

    fun getInternalSlots(): Int? {
        if (Grids != null) {
            return Grids.sumOf { it._props?.cellsH?.times(it._props.cellsV ?: 0) ?: 0 }
        }
        return null
    }

    fun getTotalSlots(): Int {
        return Width?.times(Height ?: 1) ?: 1
    }

    fun getPrice(): Int {
        return pricing?.getPrice() ?: 0
    }

    fun getPricePerSlot(price: Int = getPrice()): Int {
        return price / getTotalSlots()
    }

    fun getUpdatedTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        sdf.timeZone = TimeZone.getTimeZone("GMT")
        return "Updated ${
            DateUtils.getRelativeTimeSpanString(
                sdf.parse(pricing?.updated ?: "2021-07-01T08:36:35.194Z")?.time ?: 0,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            )
        }"
    }

    fun getAllMods(): List<String>? {
        if (Slots.isNullOrEmpty()) return null
        return Slots.flatMap {
            it._props?.filters?.first()?.Filter?.filterNotNull()!!
        }
    }

    fun destructibility(): Double {
        return when (ArmorMaterial) {
            "UHMWPE" -> 0.45
            "Titan" -> 0.55
            "Glass" -> 0.8
            "Combined" -> 0.5
            "Ceramic" -> 0.8
            "ArmoredSteel" -> 0.7
            "Aramid" -> 0.25
            "Aluminium" -> 0.6
            else -> 0.0
        }
    }

    fun materialName(): String {
        return when (ArmorMaterial) {
            "UHMWPE" -> ArmorMaterial
            "Titan" -> ArmorMaterial
            "Glass" -> ArmorMaterial
            "Combined" -> "Combined Materials"
            "Ceramic" -> ArmorMaterial
            "ArmoredSteel" -> "Steel"
            "Aramid" -> ArmorMaterial
            "Aluminium" -> ArmorMaterial
            else -> "$ArmorMaterial"
        }
    }
}

fun JSONObject.toItem(): Item {
    val props = getJSONObject("_props")

    val itemType = getItemType()

    val builder = GsonBuilder()

    val item = builder.create().fromJson(props.toString(), Item::class.java)


    item.itemType = itemType
    item.parent = optString("_parent")
    item.id = getString("_id") ?: ""

    if (props.has("effects_damage") && props["effects_damage"] is JSONObject) {
        Timber.d(props.getJSONObject("effects_damage").toString())
    }

    item.effects_health = if (props.has("effects_health") && props["effects_health"] is JSONObject) {
        props.getJSONObject("effects_health")
    } else {
        null
    }

    item.effects_damage = if (props.has("effects_damage") && props["effects_damage"] is JSONObject) {
        props.getJSONObject("effects_damage")
    } else {
        null
    }

    return item
}
package com.austinhodak.tarkovapi.room.models

import android.text.format.DateUtils
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.austinhodak.tarkovapi.room.enums.ItemTypes
import com.austinhodak.tarkovapi.utils.getItemType
import com.google.gson.Gson
import org.json.JSONObject
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
    val Grids: List<Grid>? = null
) {
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
        return pricing?.lastLowPrice ?: pricing?.basePrice ?: 0
    }

    fun getPricePerSlot(): Int {
        return getPrice() / getTotalSlots()
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
}

fun JSONObject.toItem(): Item {
    val props = getJSONObject("_props")

    val itemType = getItemType()

    val item = Gson().fromJson(props.toString(), Item::class.java)
    item.itemType = itemType
    item.parent = optString("_parent")
    item.id = getString("_id") ?: ""

    return item

    return Item(
        id = getString("_id") ?: "",
        parent = optString("_parent"),
        Name = props.optString("Name"),
        ShortName = props.optString("ShortName"),
        Description = props.optString("Description"),
        Weight = props.optDouble("Weight"),
        itemType = itemType,
        Width = props.optInt("Width"),
        Height = props.optInt("Height"),
        StackMaxSize = props.optInt("StackMaxSize"),
        Rarity = props.optString("Rarity"),
        SpawnChance = props.optDouble("SpawnChance"),
        BackgroundColor = props.optString("BackgroundColor"),
        LootExperience = props.optInt("LootExperience"),
        ExamineExperience = props.optInt("ExamineExperience"),
        RepairCost = props.optInt("RepairCost"),
        Durability = props.optInt("Durability"),
        MaxDurability = props.optInt("MaxDurability"),
    )
}
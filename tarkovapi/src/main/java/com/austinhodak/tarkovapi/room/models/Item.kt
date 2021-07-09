package com.austinhodak.tarkovapi.room.models

import android.text.format.DateUtils
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.austinhodak.tarkovapi.fragment.ItemFragment
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


@Entity(tableName = "items")
data class Item (
    @PrimaryKey val id: String,
    val itemType: ItemType? = ItemType.NONE,
    val parent: String? = null,
    val name: String? = null,
    val shortName: String? = null,
    val description: String? = null,
    val weight: Double? = null,
    val width: Int? = null,
    val height: Int? = null,
    val StackMaxSize: Int? = null,
    val Rarity: String? = null,
    val SpawnChance: Int? = null,
    val BackgroundColor: String? = null,
    val LootExperience: Int? = null,
    val ExamineExperience: Int? = null,
    val RepairCost: Int? = null,
    val Durability: Int? = null,
    val MaxDurability: Int? = null,
    val pricing: ItemFragment? = null
    //@Embedded val prefab: Prefab?,
) {
    fun getTotalSlots(): Int {
        return width?.times(height ?: 1) ?: 1
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
        return "Updated ${DateUtils.getRelativeTimeSpanString(sdf.parse(pricing?.updated ?: "2021-07-01T08:36:35.194Z")?.time ?: 0, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)}"
    }
}

fun toItem(item: JSONObject): Item {
    val props = item.getJSONObject("_props")

    val itemType: ItemType = when {
        props.has("weapFireType") -> ItemType.WEAPON
        props.has("Prefab") && props.getJSONObject("Prefab").getString("path").contains("assets/content/items/mods") -> ItemType.MODS
        props.has("Caliber") -> ItemType.AMMO
        else -> ItemType.NONE
    }

    return Item (
        id = item.getString("_id") ?: "",
        parent = item.optString("_parent"),
        name = props.optString("Name"),
        shortName = props.optString("ShortName"),
        description = props.optString("Description"),
        weight = props.optDouble("Weight"),
        itemType = itemType,
        width = props.optInt("Width"),
        height = props.optInt("Height"),
        StackMaxSize = props.optInt("StackMaxSize"),
        Rarity = props.optString("Rarity"),
        SpawnChance = props.optInt("SpawnChance"),
        BackgroundColor = props.optString("BackgroundColor"),
        LootExperience = props.optInt("LootExperience"),
        ExamineExperience = props.optInt("ExamineExperience"),
        RepairCost = props.optInt("RepairCost"),
        Durability = props.optInt("Durability"),
        MaxDurability = props.optInt("MaxDurability"),
    )
}

enum class ItemType {
    NONE,
    AMMO,
    ARMOR,
    BACKPACK,
    GLASSES,
    GRENADE,
    GUN,
    HELMET,
    KEY,
    MODS,
    PROVISIONS,
    WEARABLE,
    WEAPON
}
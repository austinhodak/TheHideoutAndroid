package com.austinhodak.tarkovapi.room.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import org.json.JSONObject


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
    val MaxDurability: Int? = null
    //@Embedded val prefab: Prefab?,
) {
    fun getTotalSlots(): Int {
        return width?.times(height ?: 1) ?: 1
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

data class ItemWithPriceItem(
    @Embedded val item: Item,
    @Relation(
        parentColumn = "id",
        entityColumn = "id"
    )
    val priceItem: PriceItem
)
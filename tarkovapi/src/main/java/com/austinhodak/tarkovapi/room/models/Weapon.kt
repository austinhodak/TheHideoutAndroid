package com.austinhodak.tarkovapi.room.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import java.lang.reflect.Type

@Entity(tableName = "weapons")
data class Weapon (
    @PrimaryKey val id: String,
    val itemType: ItemType? = ItemType.NONE,
    val parent: String?,
    val name: String?,
    val shortName: String?,
    val description: String?,
    val weight: Double? = null,
    val slots: List<Filter>?,
    @Embedded val ballistics: Ballistics? = null
) {
    data class Ballistics (
        val weapClass: String?,
        val weapUseType: String?,
        val ammoCaliber: String?,
        val weapFireType: List<String>?,
        val firerate: Int?,
        val Ergonomics: Int?,
        val Velocity: Int?,
        val magType: String?,
        val ammo: String?,
        val chambers: List<Filter>?,
        val reloadMode: String?,
        val burstShotsCount: Int?,
    )
}

data class Filter(
    val _id: String,
    val _mergeSlotWithChildren: Boolean,
    val _name: String,
    val _parent: String,
    //val _props: Props,
    val _proto: String,
    val _required: Boolean
) {
    /*data class Props(
       val filters: List<Filter>
    ) {
        data class Filter(
            val Filter: List<String>,
            val Shift: Int
        )
    }*/
}

fun toWeaponItem(item: JSONObject): Weapon {
    val props = item.getJSONObject("_props")
    val filterType: Type = object : TypeToken<ArrayList<Filter?>?>() {}.type

    val itemType: ItemType = when {
        props.has("weapFireType") -> ItemType.WEAPON
        props.has("Prefab") && props.getJSONObject("Prefab").getString("path").contains("assets/content/items/mods") -> ItemType.MODS
        props.has("Caliber") -> ItemType.AMMO
        else -> ItemType.NONE
    }

    return Weapon (
        id = item.getString("_id"),
        parent = item.optString("_parent"),
        name = props.optString("Name"),
        shortName = props.optString("ShortName"),
        description = props.optString("Description"),
        weight = props.optDouble("Weight"),
        itemType = itemType,
        slots = Gson().fromJson(props.optJSONArray("Slots")?.toString() ?: "", filterType),
        ballistics = Weapon.Ballistics(
            props.optString("weapClass"),
            props.optString("weapUseType"),
            props.optString("ammoCaliber"),
            List(props.optJSONArray("weapFireType")?.length() ?: 0) {
                props.optJSONArray("weapFireType")?.optString(it) ?: ""
            },
            props.optInt("bFirerate"),
            props.optInt("Ergonomics"),
            props.optInt("Velocity"),
            props.optString("defMagType"),
            props.optString("defAmmo"),
            Gson().fromJson(props.optJSONArray("Chambers")?.toString(), filterType),
            props.optString("ReloadMode"),
            props.optInt("BurstShotsCount"),
        )
    )
}
package com.austinhodak.tarkovapi.room.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.austinhodak.tarkovapi.room.enums.ItemType
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.tarkovapi.utils.getItemType
import org.json.JSONObject

@Entity(tableName = "ammo")
data class Ammo (
    @PrimaryKey val id: String,
    val itemType: ItemType? = ItemType.NONE,
    val parent: String?,
    val name: String?,
    val shortName: String?,
    val description: String?,
    val weight: Double? = null,
    val Caliber: String?,
    val pricing: Pricing? = null,
    @Embedded val ballistics: Ballistics? = null
) {
    fun getPrice(): String {
        return (pricing?.lastLowPrice ?: pricing?.basePrice ?: 0).asCurrency()
    }

    data class Ballistics (
        val damage: Int,
        val armorDamage: Int,
        val fragmentationChance: Double,
        val ricochetChance: Double,
        val penetrationChance: Double,
        val penetrationPower: Double,
        val accuracy: Double,
        val recoil: Double,
        val initialSpeed: Int,
        val tracer: Boolean,
        val tracerColor: String,
        val ammoType: String,
        val projectileCount: Int
    )
}

fun JSONObject.toAmmoItem(): Ammo {
    val props = getJSONObject("_props")

    val itemType = getItemType()

    return Ammo (
        id = getString("_id"),
        parent = optString("_parent"),
        name = props.optString("Name"),
        shortName = props.optString("ShortName"),
        description = props.optString("Description"),
        weight = props.optDouble("Weight"),
        itemType = itemType,
        Caliber = props.optString("Caliber"),
        ballistics = Ammo.Ballistics(
            props.optInt("Damage"),
            props.optInt("ArmorDamage"),
            props.optDouble("FragmentationChance"),
            props.optDouble("RicochetChance"),
            props.optDouble("PenetrationChance"),
            props.optDouble("PenetrationPower"),
            props.optDouble("ammoAccr"),
            props.optDouble("ammoRec"),
            props.optInt("InitialSpeed"),
            props.optBoolean("Tracer"),
            props.optString("TracerColor"),
            props.optString("ammoType"),
            props.optInt("ProjectileCount"),
        )
    )
}
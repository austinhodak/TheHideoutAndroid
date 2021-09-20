package com.austinhodak.tarkovapi.room.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.austinhodak.tarkovapi.room.enums.ItemTypes

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
)
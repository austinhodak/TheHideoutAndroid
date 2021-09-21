package com.austinhodak.tarkovapi.room

import androidx.room.TypeConverter
import com.austinhodak.tarkovapi.fragment.RepFragment
import com.austinhodak.tarkovapi.fragment.TraderFragment
import com.austinhodak.tarkovapi.room.enums.ItemTypes
import com.austinhodak.tarkovapi.room.models.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import java.lang.reflect.Type
import java.util.*

class Converters {

    @TypeConverter
    fun toObject(value: String?): JSONObject? {
        return value?.let {
            try {
                JSONObject(value)
            } catch (e: Exception) {
                null
            }
        }
    }

    @TypeConverter
    fun fromObject(value: JSONObject?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toItemType(value: String?): ItemTypes? {
        return value?.let { ItemTypes.valueOf(value) }
    }

    @TypeConverter
    fun fromItemType(value: ItemTypes?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toPricing(value: String?): Pricing? {
        if (value == null) return null
        return Gson().fromJson(value, Pricing::class.java)
    }

    @TypeConverter
    fun fromPricing(value: Pricing?): String? {
        if (value == null) return null
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toCartridge(value: String?): List<Mod.Cartridge>? {
        val filterType: Type = object : TypeToken<ArrayList<Mod.Cartridge>?>() {}.type
        if (value == null) return null
        return Gson().fromJson(value, filterType)
    }

    @TypeConverter
    fun fromCartridge(value: List<Mod.Cartridge>?): String? {
        if (value == null) return null
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toChamber(value: String?): List<Weapon.Chamber>? {
        val filterType: Type = object : TypeToken<ArrayList<Weapon.Chamber>?>() {}.type
        if (value == null) return null
        return Gson().fromJson(value, filterType)
    }

    @TypeConverter
    fun fromChamber(value: List<Weapon.Chamber>?): String? {
        if (value == null) return null
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toSlot(value: String?): List<Weapon.Slot>? {
        val filterType: Type = object : TypeToken<ArrayList<Weapon.Slot>?>() {}.type
        if (value == null) return null
        return Gson().fromJson(value, filterType)
    }

    @TypeConverter
    fun fromSlot(value: List<Weapon.Slot>?): String? {
        if (value == null) return null
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toGrid(value: String?): List<Grid>? {
        val filterType: Type = object : TypeToken<ArrayList<Grid>?>() {}.type
        if (value == null) return null
        return Gson().fromJson(value, filterType)
    }

    @TypeConverter
    fun fromCraftItem(value: List<Craft.CraftItem>?): String? {
        if (value == null) return null
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toCraftItem(value: String?): List<Craft.CraftItem>? {
        val filterType: Type = object : TypeToken<ArrayList<Craft.CraftItem>?>() {}.type
        if (value == null) return null
        return Gson().fromJson(value, filterType)
    }

    @TypeConverter
    fun fromGrid(value: List<Grid>?): String? {
        if (value == null) return null
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringArray(value: String?): List<String>? {
        return value?.split(",")
    }

    @TypeConverter
    fun fromStringArray(value: List<String>?): String? {
        return value?.joinToString(",")
    }

    @TypeConverter
    fun toTraderFragment(value: String?): TraderFragment? {
        return Gson().fromJson(value, TraderFragment::class.java)
    }

    @TypeConverter
    fun fromTraderFragment(value: TraderFragment?): String? {
        if (value == null) return null
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toRepFragment(value: String?): List<RepFragment>? {
        val filterType: Type = object : TypeToken<ArrayList<RepFragment>?>() {}.type
        if (value == null) return null
        return Gson().fromJson(value, filterType)
    }

    @TypeConverter
    fun fromRepFragment(value: List<RepFragment>?): String? {
        if (value == null) return null
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toObjectiveFragment(value: String?): List<Quest.QuestObjective>? {
        val filterType: Type = object : TypeToken<ArrayList<Quest.QuestObjective>?>() {}.type
        if (value == null) return null
        return Gson().fromJson(value, filterType)
    }

    @TypeConverter
    fun fromObjectiveFragment(value: List<Quest.QuestObjective>?): String? {
        if (value == null) return null
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toQuestReq(value: String?): Quest.QuestRequirement? {
        if (value == null) return null
        return Gson().fromJson(value, Quest.QuestRequirement::class.java)
    }

    @TypeConverter
    fun fromQuestReq(value: Quest.QuestRequirement?): String? {
        if (value == null) return null
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toReqList(value: String?): List<List<Int>>? {
        val filterType: Type = object : TypeToken<ArrayList<List<Int>>?>() {}.type
        if (value == null) return null
        return Gson().fromJson(value, filterType)
    }

    @TypeConverter
    fun fromReqList(value: List<List<Int>>?): String? {
        if (value == null) return null
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toIntList(value: String?): List<Int>? {
        val filterType: Type = object : TypeToken<ArrayList<Int>?>() {}.type
        if (value == null) return null
        return Gson().fromJson(value, filterType)
    }

    @TypeConverter
    fun fromIntList(value: List<Int>?): String? {
        if (value == null) return null
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toReqQuests(value: String?): List<List<Quest>>? {
        val filterType: Type = object : TypeToken<ArrayList<List<Quest>>?>() {}.type
        if (value == null) return null
        return Gson().fromJson(value, filterType)
    }

    @TypeConverter
    fun fromReqQuests(value: List<List<Quest>>?): String? {
        if (value == null) return null
        return Gson().toJson(value)
    }

}
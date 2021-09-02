package com.austinhodak.tarkovapi.room

import androidx.room.TypeConverter
import com.austinhodak.tarkovapi.fragment.ObjectiveFragment
import com.austinhodak.tarkovapi.fragment.QuestFragment
import com.austinhodak.tarkovapi.fragment.RepFragment
import com.austinhodak.tarkovapi.fragment.TraderFragment
import com.austinhodak.tarkovapi.room.enums.ItemTypes
import com.austinhodak.tarkovapi.room.models.Grid
import com.austinhodak.tarkovapi.room.models.Pricing
import com.austinhodak.tarkovapi.room.models.Quest
import com.austinhodak.tarkovapi.room.models.Weapon
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class Converters {

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
    fun toObjectiveFragment(value: String?): List<ObjectiveFragment>? {
        val filterType: Type = object : TypeToken<ArrayList<ObjectiveFragment>?>() {}.type
        if (value == null) return null
        return Gson().fromJson(value, filterType)
    }

    @TypeConverter
    fun fromObjectiveFragment(value: List<ObjectiveFragment>?): String? {
        if (value == null) return null
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toQuestReq(value: String?): QuestFragment.Requirements? {
        if (value == null) return null
        return Gson().fromJson(value, QuestFragment.Requirements::class.java)
    }

    @TypeConverter
    fun fromQuestReq(value: QuestFragment.Requirements?): String? {
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
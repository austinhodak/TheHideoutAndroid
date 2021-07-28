package com.austinhodak.tarkovapi.room

import androidx.room.TypeConverter
import com.austinhodak.tarkovapi.fragment.*
import com.austinhodak.tarkovapi.room.models.Filter
import com.austinhodak.tarkovapi.room.models.ItemType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class Converters {

    @TypeConverter
    fun toItemType(value: String?): ItemType? {
        return value?.let { ItemType.valueOf(value) }
    }

    @TypeConverter
    fun fromItemType(value: ItemType?): String? {
        return value?.toString()
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
    fun toTT(value: String?): ItemFragment? {
        if (value == null) return null
        return Gson().fromJson(value, ItemFragment::class.java)
    }

    @TypeConverter
    fun fromTT(value: ItemFragment?): String? {
        if (value == null) return null
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toTraderFragment(value: String?): TraderFragment? {
        if (value == null) return null
        return Gson().fromJson(value, TraderFragment::class.java)
    }

    @TypeConverter
    fun fromTraderFragment(value: TraderFragment?): String? {
        if (value == null) return null
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toFilter(value: String?): Filter? {
        if (value == null) return null
        return Gson().fromJson(value, Filter::class.java)
    }

    @TypeConverter
    fun fromFilter(value: Filter?): String? {
        if (value == null) return null
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toFilter3(value: String?): List<Filter>? {
        val filterType: Type = object : TypeToken<ArrayList<Filter?>?>() {}.type
        if (value == null) return null
        return Gson().fromJson(value, filterType)
    }

    @TypeConverter
    fun fromFilter3(value: List<Filter>?): String? {
        if (value == null) return null
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toTaskItemList(value: String?): List<TaskItem>? {
        val filterType: Type = object : TypeToken<ArrayList<TaskItem>?>() {}.type
        if (value == null) return null
        return Gson().fromJson(value, filterType)
    }

    @TypeConverter
    fun fromTaskItemList(value: List<TaskItem>?): String? {
        if (value == null) return null
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toRepFragmentList(value: String?): List<RepFragment>? {
        val filterType: Type = object : TypeToken<ArrayList<RepFragment>?>() {}.type
        if (value == null) return null
        return Gson().fromJson(value, filterType)
    }

    @TypeConverter
    fun fromRepFragmentList(value: List<RepFragment>?): String? {
        if (value == null) return null
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toRepFragment(value: String?): RepFragment? {
        val filterType: Type = object : TypeToken<RepFragment?>() {}.type
        if (value == null) return null
        return Gson().fromJson(value, filterType)
    }

    @TypeConverter
    fun fromRepFragment(value: RepFragment?): String? {
        if (value == null) return null
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toObjFragmentList(value: String?): List<ObjectiveFragment>? {
        val filterType: Type = object : TypeToken<ArrayList<ObjectiveFragment>?>() {}.type
        if (value == null) return null
        return Gson().fromJson(value, filterType)
    }


    @TypeConverter
    fun fromObjFragmentList(value: List<ObjectiveFragment>?): String? {
        if (value == null) return null
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toObjFragment(value: String?): ObjectiveFragment? {
        val filterType: Type = object : TypeToken<ObjectiveFragment?>() {}.type
        if (value == null) return null
        return Gson().fromJson(value, filterType)
    }

    @TypeConverter
    fun fromObjFragment(value: ObjectiveFragment?): String? {
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

}
package com.austinhodak.tarkovapi.room

import androidx.room.TypeConverter
import com.austinhodak.tarkovapi.fragment.ItemFragment
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
    fun toFilter(value: String?): Filter? {
        if (value == null) return null
        return Gson().fromJson(value, Filter::class.java)
    }

    @TypeConverter
    fun fromFilter(value: Filter?): String? {
        if (value == null) return null
        return Gson().toJson(value)
    }

   /* @TypeConverter
    fun toProps(value: String?): Filter.Props? {
        if (value == null) return null
        return Gson().fromJson(value, Filter.Props::class.java)
    }

    @TypeConverter
    fun fromProps(value: Filter.Props?): String? {
        if (value == null) return null
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toFilter2(value: String?): Filter.Props.Filter? {
        if (value == null) return null
        return Gson().fromJson(value, Filter.Props.Filter::class.java)
    }

    @TypeConverter
    fun fromFilter2(value: Filter.Props.Filter?): String? {
        if (value == null) return null
        return Gson().toJson(value)
    }*/

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

}
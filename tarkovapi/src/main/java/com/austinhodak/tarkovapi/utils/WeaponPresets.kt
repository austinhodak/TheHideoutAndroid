package com.austinhodak.tarkovapi.utils

import android.content.Context
import com.austinhodak.tarkovapi.R
import com.austinhodak.tarkovapi.models.WeaponPreset
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class WeaponPresets(context: Context) {

    var presets: MutableList<WeaponPreset> = mutableListOf()

    init {
        loadPresets(context)
    }

    private fun loadPresets(context: Context) {
        if (presets.isNullOrEmpty()) {
            val groupListType: Type = object : TypeToken<ArrayList<WeaponPreset?>?>() {}.type
            presets = Gson().fromJson(context.resources.openRawResource(R.raw.weapon_presets).bufferedReader().use { it.readText() }, groupListType)
        }
    }

    fun getPreset(id: String?): WeaponPreset? = presets.find { it.id == id }

    fun getPresetsForWeapon(id: String): List<WeaponPreset> = presets.filter { it.baseId == id }
    fun getPresetsWithMod(id: String): List<WeaponPreset> = presets.filter { it.parts.any { it.id == id } }
}
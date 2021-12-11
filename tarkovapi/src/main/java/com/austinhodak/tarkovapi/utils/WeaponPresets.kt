package com.austinhodak.tarkovapi.utils

import android.content.Context
import com.austinhodak.tarkovapi.R
import com.austinhodak.tarkovapi.models.Map
import com.austinhodak.tarkovapi.models.MapInteractive
import com.austinhodak.tarkovapi.models.Rig
import com.austinhodak.tarkovapi.models.WeaponPreset
import com.austinhodak.tarkovapi.room.models.Quest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import java.lang.reflect.Type

class WeaponPresets(context: Context) {

    var presets: MutableList<WeaponPreset> = mutableListOf()

    init {
        //loadMaps(context)
    }

    private fun loadMaps(context: Context) {
        if (presets.isNullOrEmpty()) {
            val groupListType: Type = object : TypeToken<ArrayList<WeaponPreset?>?>() {}.type
            presets = Gson().fromJson(context.resources.openRawResource(R.raw.weapon_presets).bufferedReader().use { it.readText() }, groupListType)
        }
    }

    fun getPreset(id: String?): WeaponPreset? = presets.find { it.id == id }
}
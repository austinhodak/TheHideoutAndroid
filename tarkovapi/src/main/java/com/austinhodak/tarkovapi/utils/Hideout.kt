package com.austinhodak.tarkovapi.utils

import android.content.Context
import com.austinhodak.tarkovapi.R
import com.austinhodak.tarkovapi.models.Hideout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class Hideout(context: Context) {

    var hideout: Hideout? = null

    init {
        loadHideout(context)
    }

    private fun loadHideout(context: Context) {
        if (hideout == null) {
            val modules = context.resources.openRawResource(R.raw.hideout_modules).bufferedReader().use { it.readText() }
            val stations = context.resources.openRawResource(R.raw.hideout_stations).bufferedReader().use { it.readText() }
            val moduleTypes: Type = object : TypeToken<ArrayList<Hideout.Module?>?>() {}.type
            val stationTypes: Type = object : TypeToken<ArrayList<Hideout.Station?>?>() {}.type
            hideout = Hideout(
                modules = Gson().fromJson(modules, moduleTypes),
                stations = Gson().fromJson(stations, stationTypes)
            )
        }
    }
}
package com.austinhodak.tarkovapi.utils

import android.content.Context
import com.austinhodak.tarkovapi.R
import com.austinhodak.tarkovapi.models.Map
import com.austinhodak.tarkovapi.models.MapInteractive
import com.austinhodak.tarkovapi.room.models.Quest
import com.google.gson.Gson
import org.json.JSONObject

class Maps(context: Context) {

    var maps: MutableList<Map> = mutableListOf()

    init {
        loadMaps(context)
    }

    private fun loadMaps(context: Context) {
        if (maps.isNullOrEmpty()) {
            val string = context.resources.openRawResource(R.raw.maps).bufferedReader().use { it.readText() }
            val json = JSONObject(string)
            maps.add(Gson().fromJson(json.getJSONObject("factory").toString(), Map::class.java))
            maps.add(Gson().fromJson(json.getJSONObject("customs").toString(), Map::class.java))
            maps.add(Gson().fromJson(json.getJSONObject("woods").toString(), Map::class.java))
            maps.add(Gson().fromJson(json.getJSONObject("shoreline").toString(), Map::class.java))
            maps.add(Gson().fromJson(json.getJSONObject("interchange").toString(), Map::class.java))
            maps.add(Gson().fromJson(json.getJSONObject("lab").toString(), Map::class.java))
            maps.add(Gson().fromJson(json.getJSONObject("reserve").toString(), Map::class.java))
        }
    }

    fun getMap(id: Int?): Map? = maps.find { it.id == id }
    fun getMap(string: String): Map? = maps.find { it.locale?.en == string }


}
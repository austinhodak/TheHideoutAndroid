package com.austinhodak.tarkovapi.utils

import android.content.Context
import com.austinhodak.tarkovapi.R
import com.austinhodak.tarkovapi.models.Map
import com.austinhodak.tarkovapi.models.MapInteractive
import com.austinhodak.tarkovapi.models.Rig
import com.austinhodak.tarkovapi.room.models.Quest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import java.lang.reflect.Type

class Rigs(context: Context) {

    var rigs: MutableList<Rig> = mutableListOf()

    init {
        loadMaps(context)
    }

    private fun loadMaps(context: Context) {
        if (rigs.isNullOrEmpty()) {
            val groupListType: Type = object : TypeToken<ArrayList<Rig?>?>() {}.type
            rigs = Gson().fromJson(context.resources.openRawResource(R.raw.rig_images).bufferedReader().use { it.readText() }, groupListType)
        }
    }

    fun getRig(id: String?): Rig? = rigs.find { it.id == id }
}
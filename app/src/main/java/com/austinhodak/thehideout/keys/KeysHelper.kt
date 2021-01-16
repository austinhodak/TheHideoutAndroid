package com.austinhodak.thehideout.keys

import android.content.Context
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.viewmodels.models.Key
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.json.JSONArray
import java.lang.reflect.Type

object KeysHelper {
    private var objectString: String? = null
    private var list: List<Key>? = null

    fun getKeys(context: Context): List<Key> {
        if (objectString == null) {
            objectString = context.resources.openRawResource(R.raw.keys).bufferedReader().use { it.readText() }
        }
        val map = JSONArray(objectString)
        val groupListType: Type = object : TypeToken<ArrayList<Key?>?>() {}.type
        if (list == null) {
            list = Gson().fromJson(map.toString(), groupListType)
        }
        return list!!.sortedBy { it.name }
    }

    fun getKeys(context: Context, klaxon: Boolean): List<Key> {
        return Json.decodeFromString( context.resources.openRawResource(R.raw.keys).bufferedReader().use { it.readText() } )
    }
}
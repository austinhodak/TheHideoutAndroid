package com.austinhodak.thehideout.clothing.rigs

import android.content.Context
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.viewmodels.models.Rig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import java.lang.reflect.Type

object RigHelper {
    private var objectString: String? = null
    private var list: List<Rig>? = null

    fun getRigs(context: Context): List<Rig> {
        if (objectString == null) {
            objectString = context.resources.openRawResource(R.raw.rigs).bufferedReader().use { it.readText() }
        }
        val map = JSONArray(objectString)
        val groupListType: Type = object : TypeToken<ArrayList<Rig?>?>() {}.type
        if (list == null) {
            list = Gson().fromJson(map.toString(), groupListType)
        }
        return list!!.sortedBy { it.name }
    }
}
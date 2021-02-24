package com.austinhodak.thehideout.clothing.backpacks

import android.content.Context
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.clothing.models.Backpack
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import java.lang.reflect.Type

object BackpackHelper {
    private var objectString: String? = null
    private var list: List<Backpack>? = null

    fun getBackpacks(context: Context): List<Backpack> {
        if (objectString == null) {
            objectString = context.resources.openRawResource(R.raw.backpacks).bufferedReader().use { it.readText() }
        }
        val map = JSONArray(objectString)
        val groupListType: Type = object : TypeToken<ArrayList<Backpack?>?>() {}.type
        if (list == null) {
            list = Gson().fromJson(map.toString(), groupListType)
        }
        return list!!.sortedBy { it.name }
    }
}
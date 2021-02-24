package com.austinhodak.thehideout.clothing.armor

import android.content.Context
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.clothing.models.Armor
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import java.lang.reflect.Type

object ArmorHelper {
    private var objectString: String? = null
    private var list: List<Armor>? = null

    fun getArmors(context: Context): List<Armor> {
        if (objectString == null) {
            objectString = context.resources.openRawResource(R.raw.armor).bufferedReader().use { it.readText() }
        }
        val map = JSONArray(objectString)
        val groupListType: Type = object : TypeToken<ArrayList<Armor?>?>() {}.type
        if (list == null) {
            list = Gson().fromJson(map.toString(), groupListType)
        }
        return list!!.sortedBy { it.name }
    }
}
package com.austinhodak.thehideout.ammunition

import android.content.Context
import android.util.Log
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.viewmodels.models.AmmoModel
import com.austinhodak.thehideout.viewmodels.models.CaliberModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import java.lang.reflect.Type

object AmmoHelper {
    private var objectString: String? = null
    private var list: List<CaliberModel>? = null

    fun getCalibers(context: Context): List<CaliberModel> {
        if (objectString == null) {
            objectString = context.resources.openRawResource(R.raw.ammo).bufferedReader().use { it.readText() }
        }
        val map = JSONArray(objectString)
        val groupListType: Type = object : TypeToken<ArrayList<CaliberModel?>?>() {}.type
        if (list == null) {
            list = Gson().fromJson(map.toString(), groupListType)
        }
        return list!!.sortedBy { it.name }
    }

    fun getAmmoListByID(context: Context, id: String): List<AmmoModel>? {
        return getCalibers(context).find { it._id == id }?.ammo
    }
}
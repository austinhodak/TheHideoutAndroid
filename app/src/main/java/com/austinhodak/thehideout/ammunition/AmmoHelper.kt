package com.austinhodak.thehideout.ammunition

import android.content.Context
import android.util.Log
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.viewmodels.models.AmmoModel
import com.austinhodak.thehideout.viewmodels.models.CaliberModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.miguelcatalan.materialsearchview.SuggestionModel
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

    fun getCaliberByID(context: Context, id: String): CaliberModel? {
        return getCalibers(context).find { it._id == id }
    }

    fun getAllAmmoList(context: Context): MutableList<SuggestionModel> {
        val ammoList: MutableList<AmmoModel> = ArrayList()
        val stringList: MutableList<SuggestionModel> = ArrayList()
        for (item in getCalibers(context)) {
            for (ammo in item.ammo) {
                ammo.caliber = item.name
                stringList.add(SuggestionModel("${item.name} ${ammo.name}", ammo))
            }
        }

        return  stringList
    }
}
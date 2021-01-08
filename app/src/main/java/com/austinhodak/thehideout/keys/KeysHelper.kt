package com.austinhodak.thehideout.keys

import android.content.Context
import android.util.Log
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.viewmodels.models.AmmoModel
import com.austinhodak.thehideout.viewmodels.models.Armor
import com.austinhodak.thehideout.viewmodels.models.CaliberModel
import com.austinhodak.thehideout.viewmodels.models.Key
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.miguelcatalan.materialsearchview.SuggestionModel
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
}
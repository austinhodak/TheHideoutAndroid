package com.austinhodak.thehideout.ammunition

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import java.lang.reflect.Type

object AmmoHelper {

    /**
     * Gets caliber list from remote config and parses to a List of Caliber objects.
     */

    val caliberList: List<Caliber> by lazy {
        val ammoCalibers = JSONObject(Firebase.remoteConfig.getValue("ammoCalibers").asString())
        val groupListType: Type = object : TypeToken<ArrayList<Caliber?>?>() {}.type
        val list: ArrayList<Caliber> = Gson().fromJson(ammoCalibers.getJSONArray("calibers").toString(), groupListType)
        list.sortedBy { it.name }
    }

    fun getCaliberByID(id: String): Caliber? {
        return caliberList.find { it.key == id }
    }

    data class Caliber (
        var name: String,
        var longName: String,
        var key: String
    )
}
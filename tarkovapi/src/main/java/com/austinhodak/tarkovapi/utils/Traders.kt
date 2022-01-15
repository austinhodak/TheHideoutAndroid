package com.austinhodak.tarkovapi.utils

import android.content.Context
import com.austinhodak.tarkovapi.R
import com.austinhodak.tarkovapi.models.TraderInfo
import com.google.gson.Gson
import org.json.JSONObject

class Traders(context: Context) {

    var traders: MutableList<TraderInfo> = mutableListOf()

    init {
        loadTraders(context)
    }

    private fun loadTraders(context: Context) {
        if (traders.isNullOrEmpty()) {
            val string = context.resources.openRawResource(R.raw.traders).bufferedReader().use { it.readText() }
            val json = JSONObject(string)
            traders.add(Gson().fromJson(json.getJSONObject("prapor").toString(), TraderInfo::class.java))
            traders.add(Gson().fromJson(json.getJSONObject("therapist").toString(), TraderInfo::class.java))
            traders.add(Gson().fromJson(json.getJSONObject("fence").toString(), TraderInfo::class.java))
            traders.add(Gson().fromJson(json.getJSONObject("skier").toString(), TraderInfo::class.java))
            traders.add(Gson().fromJson(json.getJSONObject("peacekeeper").toString(), TraderInfo::class.java))
            traders.add(Gson().fromJson(json.getJSONObject("mechanic").toString(), TraderInfo::class.java))
            traders.add(Gson().fromJson(json.getJSONObject("ragman").toString(), TraderInfo::class.java))
            traders.add(Gson().fromJson(json.getJSONObject("jaeger").toString(), TraderInfo::class.java))
        }
    }

    fun getTrader(string: String): TraderInfo? = traders.find { it.locale.en.equals(string, true) }


}
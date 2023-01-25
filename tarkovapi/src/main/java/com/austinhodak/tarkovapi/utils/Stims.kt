package com.austinhodak.tarkovapi.utils

import android.content.Context
import com.austinhodak.tarkovapi.R
import com.austinhodak.tarkovapi.models.Skill
import com.austinhodak.tarkovapi.models.Stim
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class Stims(context: Context) {

    var stims: MutableList<Stim> = mutableListOf()

    init {
        loadStims(context)
    }

    private fun loadStims(context: Context) {
        if (stims.isNullOrEmpty()) {
            val groupListType: Type = object : TypeToken<ArrayList<Stim?>?>() {}.type
            stims = Gson().fromJson(context.resources.openRawResource(R.raw.stims).bufferedReader().use { it.readText() }, groupListType)
        }
    }

    fun getStim(id: String?): Stim? = stims.find { it.stim.equals(id, true) }
}
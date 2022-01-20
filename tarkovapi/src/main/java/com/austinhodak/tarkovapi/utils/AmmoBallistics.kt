package com.austinhodak.tarkovapi.utils

import android.content.Context
import com.austinhodak.tarkovapi.R
import com.austinhodak.tarkovapi.models.AmmoBallistic
import com.austinhodak.tarkovapi.models.Skill
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class AmmoBallistics(context: Context) {

    var ballistics: MutableList<AmmoBallistic> = mutableListOf()

    init {
        loadSkills(context)
    }

    private fun loadSkills(context: Context) {
        if (ballistics.isNullOrEmpty()) {
            val groupListType: Type = object : TypeToken<ArrayList<AmmoBallistic?>?>() {}.type
            ballistics = Gson().fromJson(context.resources.openRawResource(R.raw.ammo_ballistics).bufferedReader().use { it.readText() }, groupListType)
        }
    }

    fun getAmmo(id: String?): AmmoBallistic? = ballistics.find { it.id == id }
}
package com.austinhodak.tarkovapi.utils

import android.content.Context
import com.austinhodak.tarkovapi.R
import com.austinhodak.tarkovapi.models.Skill
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class Skills(context: Context) {

    var skills: MutableList<Skill> = mutableListOf()

    init {
        loadSkills(context)
    }

    private fun loadSkills(context: Context) {
        if (skills.isNullOrEmpty()) {
            val groupListType: Type = object : TypeToken<ArrayList<Skill?>?>() {}.type
            skills = Gson().fromJson(context.resources.openRawResource(R.raw.skills).bufferedReader().use { it.readText() }, groupListType)
        }
    }

    fun getSkill(id: String?): Skill? = skills.find { it.name.equals(id, true) }
}
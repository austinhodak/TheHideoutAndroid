package com.austinhodak.thehideout.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

private const val QUEST_COMPLETED = "QUEST_COMPLETED"
private const val QUEST_OBJECTIVE_COMPLETED = "QUEST_OBJECTIVE_COMPLETED"
private const val TRADER_LEVELS = "TRADER_LEVELS"

class Prefs(context: Context) {

    val preference: SharedPreferences = context.getSharedPreferences("quests", Context.MODE_PRIVATE)

    var traderLevels: TraderLevels
        get() = Gson().fromJson(preference.getString(TRADER_LEVELS, null), TraderLevels::class.java)
        set(value) = preference.edit().putString(TRADER_LEVELS, Gson().toJson(value)).apply()
}

data class TraderLevels(
    val prapor: Int,
)
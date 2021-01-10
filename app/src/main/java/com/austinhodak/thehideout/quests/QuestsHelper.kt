package com.austinhodak.thehideout.quests

import android.content.Context
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.quests.models.Quest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import java.lang.reflect.Type

object QuestsHelper {
    private var objectString: String? = null
    private var list: List<Quest>? = null

    fun getQuests(context: Context): List<Quest> {
        if (objectString == null) {
            objectString = context.resources.openRawResource(R.raw.quests).bufferedReader().use { it.readText() }
        }
        val map = JSONArray(objectString)
        val groupListType: Type = object : TypeToken<ArrayList<Quest?>?>() {}.type
        if (list == null) {
            list = Gson().fromJson(map.toString(), groupListType)
        }
        return list!!
    }

    fun totalQuests(): Int {
        return list!!.size
    }

    fun totalPMCEliminations(): Int {
        var count = 0
        for (quest in list!!) {
            for (objective in quest.objectives) {
                if (objective.type == "kill" && objective.target == "PMCs") {
                    count += objective.number
                }
            }
        }
        return count
    }

    fun totalScavEliminations(): Int {
        var count = 0
        for (quest in list!!) {
            for (objective in quest.objectives) {
                if (objective.type == "kill" && objective.target == "Scavs") {
                    count += objective.number
                }
            }
        }
        return count
    }

    fun totalQuestItems(): Int {
        var count = 0
        for (quest in list!!) {
            for (objective in quest.objectives) {
                if (objective.type == "find" || objective.type == "collect" && objective.target != "Dollars" && objective.target != "Euros") {
                    count += objective.number
                }
            }
        }
        return count
    }

    fun totalFIRItems(): Int {
        var count = 0
        for (quest in list!!) {
            for (objective in quest.objectives) {
                if (objective.type == "find") {
                    count += objective.number
                }
            }
        }
        return count
    }

    fun totalHandoverItems(): Int {
        var count = 0
        for (quest in list!!) {
            for (objective in quest.objectives) {
                if (objective.type == "collect" && objective.target != "Dollars" && objective.target != "Euros") {
                    count += objective.number
                }
            }
        }
        return count
    }

    fun totalPlace(): Int {
        var count = 0
        for (quest in list!!) {
            for (objective in quest.objectives) {
                if (objective.type == "place" || objective.type == "mark") {
                    count += objective.number
                }
            }
        }
        return count
    }

    fun totalPickup(): Int {
        var count = 0
        for (quest in list!!) {
            for (objective in quest.objectives) {
                if (objective.type == "pickup") {
                    count += objective.number
                }
            }
        }
        return count
    }
}
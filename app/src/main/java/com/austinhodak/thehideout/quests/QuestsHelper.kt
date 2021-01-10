package com.austinhodak.thehideout.quests

import android.content.Context
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.firebase.UserFB
import com.austinhodak.thehideout.quests.models.Quest
import com.austinhodak.thehideout.quests.models.Traders
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import java.lang.reflect.Type

object QuestsHelper {
    private var objectString: String? = null
    private lateinit var list: List<Quest>

    fun getQuests(context: Context?): List<Quest> {
        if (objectString == null) {
            objectString = context?.resources?.openRawResource(R.raw.quests)?.bufferedReader().use { it?.readText() }
        }
        val map = JSONArray(objectString)
        val groupListType: Type = object : TypeToken<ArrayList<Quest?>?>() {}.type
        if (!this::list.isInitialized) {
            list = Gson().fromJson(map.toString(), groupListType)
        }
        return list
    }

    fun getCompletedQuests(trader: Traders, quests: UserFB.UserFBQuests): List<Quest> {

        return list.filter { it.giver == trader.id && quests.completed?.containsKey("\"${it.id}\"") == true }
    }

    fun totalQuests(): Int {
        return list.size
    }

    fun getLockedQuests(trader: Traders, quests: UserFB.UserFBQuests): List<Quest> {
        val filteredList: MutableList<Quest> = ArrayList()
        val completedQuestString = quests.completed?.map { q ->
            list.find { "\"${it.id}\"" == q.key }?.title
        }

        for (quest in list.filter { it.giver == trader.id }) {
            if (quest.require.quest.isNullOrEmpty() && completedQuestString?.contains(quest.title) != true) {
                continue
            } else if (quest.require.quest.isNullOrEmpty()) {
                continue
            } else {
                for (i in quest.require.quest) {
                    if (completedQuestString?.contains(i) == false) {
                        if (filteredList.contains(quest)) {
                            continue
                        }
                        filteredList.add(quest)
                    }
                }
            }
        }

        return filteredList
    }

    fun getActiveQuests(trader: Traders, quests: UserFB.UserFBQuests): List<Quest> {
        val filteredList: MutableList<Quest> = ArrayList()
        val completedQuestString = quests.completed?.map { q ->
            list.find { "\"${it.id}\"" == q.key }?.title
        }
        for (quest in list.filter { it.giver == trader.id }) {
            if (quest.require.quest.isNullOrEmpty() && completedQuestString?.contains(quest.title) != true) {
                filteredList.add(quest)
                continue
            } else if (quest.require.quest.isNullOrEmpty()) {
                continue
            } else {
                for (i in quest.require.quest) {
                    if (completedQuestString?.contains(i) == true) {
                        filteredList.add(quest)
                    }
                }
            }
        }
        return filteredList
    }

    fun totalPMCEliminations(): Int {
        var count = 0
        for (quest in list) {
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
        for (quest in list) {
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
        for (quest in list) {
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
        for (quest in list) {
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
        for (quest in list) {
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
        for (quest in list) {
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
        for (quest in list) {
            for (objective in quest.objectives) {
                if (objective.type == "pickup") {
                    count += objective.number
                }
            }
        }
        return count
    }
}
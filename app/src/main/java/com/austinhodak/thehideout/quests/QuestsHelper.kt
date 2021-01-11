package com.austinhodak.thehideout.quests

import android.content.Context
import android.util.Log
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.firebase.UserFB
import com.austinhodak.thehideout.quests.models.Maps
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
            list = list.sortedBy { it.require.level }
        }
        return list
    }



    fun getTotalPMCEliminations(objectives: UserFB.UserFBQuestObjectives): Int {
        var total = 0
        objectives.progress?.forEach {
            for (quest in list) {
                for (obj in quest.objectives) {
                    if (obj.type == "kill" && obj.target == "PMCs" && it.key == "\"${obj.id}\"") {
                        total += it.value
                    }
                }
            }
        }
        return total
    }

    fun getTotalScavEliminations(objectives: UserFB.UserFBQuestObjectives): Int {
        var total = 0
        objectives.progress?.forEach {
            for (quest in list) {
                for (obj in quest.objectives) {
                    if (obj.type == "kill" && obj.target == "Scavs" && it.key == "\"${obj.id}\"") {
                        total += it.value
                    }
                }
            }
        }
        return total
    }

    fun getTotalQuestItemsCompleted(objectives: UserFB.UserFBQuestObjectives): Int {
        var count = 0
        objectives.progress?.forEach {
            for (quest in list) {
                for (objective in quest.objectives) {
                    if (it.key == "\"${objective.id}\"" && (objective.type == "find" || objective.type == "collect" && objective.target != "Dollars" && objective.target != "Euros")) {
                        count += it.value
                    }
                }
            }
        }
        return count
    }

    fun getTotalFIRItemsCompleted(objectives: UserFB.UserFBQuestObjectives): Int {
        var count = 0
        objectives.progress?.forEach {
            for (quest in list) {
                for (objective in quest.objectives) {
                    if (objective.type == "find" && it.key == "\"${objective.id}\"") {
                        count += it.value
                    }
                }
            }
        }

        return count
    }

    fun getTotalHandoverItemCompleted(objectives: UserFB.UserFBQuestObjectives): Int {
        var count = 0
        objectives.progress?.forEach {
            for (quest in list) {
                for (objective in quest.objectives) {
                    if (it.key == "\"${objective.id}\"" && (objective.type == "collect" && objective.target != "Dollars" && objective.target != "Euros")) {
                        count += it.value
                    }
                }
            }
        }

        return count
    }

    fun getTotalPlacedCompleted(objectives: UserFB.UserFBQuestObjectives): Int {
        var count = 0
        objectives.progress?.forEach {
            for (quest in list) {
                for (objective in quest.objectives) {
                    if (it.key == "\"${objective.id}\"" && (objective.type == "place" || objective.type == "mark")) {
                        count += it.value
                    }
                }
            }
        }

        return count
    }

    fun getTotalPickupCompleted(objectives: UserFB.UserFBQuestObjectives): Int {
        var count = 0
        objectives.progress?.forEach {
            for (quest in list) {
                for (objective in quest.objectives) {
                    if (objective.type == "pickup" && it.key == "\"${objective.id}\"") {
                        count += it.value
                    }
                }
            }
        }

        return count
    }

    fun getCompletedQuests(trader: Traders? = null, quests: UserFB.UserFBQuests, map: Maps? = null): List<Quest> {
        return list.filter {
            if (map == null) {
                it.giver == trader?.id
            } else {
                it.getLocation().contains(map.id)
            } && quests.completed?.containsKey("\"${it.id}\"") == true }
    }

    fun getAllCompletedQuests(quests: UserFB.UserFBQuests): List<Quest> {
        return list.filter { quests.completed?.containsKey("\"${it.id}\"") == true }
    }

    fun totalQuests(): Int {
        return list.size
    }

    fun getLockedQuests(trader: Traders? = null, quests: UserFB.UserFBQuests, map: Maps? = null): List<Quest> {
        val filteredList: MutableList<Quest> = ArrayList()
        val completedQuestString = quests.completed?.map { q ->
            list.find { "\"${it.id}\"" == q.key }?.title
        }

        for (quest in list.filter {
            if (map == null) {
                it.giver == trader?.id
            } else {
                it.getLocation().contains(map.id)
            }
        }) {
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

    fun getLockedQuests(quests: UserFB.UserFBQuests): List<Quest> {
        val filteredList: MutableList<Quest> = ArrayList()
        val completedQuestString = quests.completed?.map { q ->
            list.find { "\"${it.id}\"" == q.key }?.title
        }

        for (quest in list) {
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

    fun getActiveQuests(trader: Traders? = null, quests: UserFB.UserFBQuests, map: Maps? = null): List<Quest> {
        val filteredList: MutableList<Quest> = ArrayList()
        val completedQuestString = quests.completed?.map { q ->
            list.find { "\"${it.id}\"" == q.key }?.title
        }
        Log.d("QUESTS", completedQuestString.toString())
        for (quest in list.filter {
            if (map == null) {
                it.giver == trader?.id
            } else {
                it.getLocation().contains(map.id)
            }
        }) {
            if (quests.completed?.containsKey("\"${quest.id}\"") == true) continue

            var bool = false
            if (!quest.require.quest.isNullOrEmpty()) {
                for (q in quest.require.quest) {
                    //Quest String
                    if (completedQuestString?.contains(q) == false) {
                        bool = false
                        break
                    } else {
                        bool = true
                    }
                }

                if (bool) {
                    if (!filteredList.contains(quest))
                        filteredList.add(quest)
                }
            } else if (completedQuestString?.contains(quest.title) == false) {
                if (!filteredList.contains(quest))
                    filteredList.add(quest)
            }

            /*if (quest.require.quest.isNullOrEmpty() && completedQuestString?.contains(quest.title) == false) {
                if (!filteredList.contains(quest))
                    filteredList.add(quest)
                continue
            } else if (quest.require.quest.isNullOrEmpty()) {
                continue
            } else {
                for (i in quest.require.quest) {
                    if (completedQuestString?.contains(i) == true) {
                        if (!filteredList.contains(quest))
                            filteredList.add(quest)
                    }
                }
            }*/
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
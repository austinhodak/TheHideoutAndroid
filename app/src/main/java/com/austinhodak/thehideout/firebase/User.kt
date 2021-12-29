package com.austinhodak.thehideout.firebase

import com.austinhodak.tarkovapi.models.Hideout
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.room.models.Quest
import com.austinhodak.thehideout.utils.userRefTracker
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    var questObjectives: Map<String, UQuestObjective?>? = null,
    var quests: Map<String, UQuest?>? = null,
    var hideoutModules: Map<String, UHideout?>? = null,
    var hideoutObjectives: Map<String, UHideoutObjective?>? = null,
    var keysHave: Map<String, Boolean>? = null,
    var items: Map<String, UNeededItem>? = null,
    var cart: Map<String, Int>? = null,
    var ttApiKey: String? = null,
    var teams: Map<String, Boolean>? = null,
    var discordUsername: String? = null,
    var token: String? = null,
    var displayName: String? = null,
    var uid: String? = null,
    var playerLevel: Int? = null
) {
    fun getUsername(): String {
        return if (!displayName.isNullOrEmpty()) displayName!! else if (!discordUsername.isNullOrEmpty()) discordUsername!! else "$uid"
    }

    data class UNeededItem (
        var hideoutObjective: Map<String, Int>? = null,
        var questObjective: Map<String, Int>? = null,
        var user: Map<String, UItemsUser>? = null,
        var has: Int? = 0
    ) {
        data class UItemsUser (
            var quantity: Int? = null,
            var reason: String? = null
        )

        fun getTotalNeeded (): Int {
            val hideoutTotal = hideoutObjective?.entries?.sumOf { it.value } ?: 0
            val questObjectiveTotal = questObjective?.entries?.sumOf { it.value } ?: 0
            val userTotal = user?.entries?.sumOf {
                it.value.quantity ?: 0
            } ?: 0

            return hideoutTotal + questObjectiveTotal + userTotal
        }
    }

    data class UQuestObjective(
        var progress: Int? = null,
        var id: Int? = null
    )

    data class UQuest(
        var id: Int? = null,
        var completed: Boolean? = null
    )

    data class UHideout(
        var id: Int? = null,
        var complete: Boolean? = null
    )

    data class UHideoutObjective(
        var progress: Int? = null,
        var id: Int? = null
    )

    fun completedHideoutIDs(): List<Int> {
        val ids: MutableList<Int> = arrayListOf()
        hideoutModules?.values?.forEach {
            if (it?.complete == true) {
                it.id?.let { ids.add(it) }
            }
        }
        return ids
    }

    fun isHideoutModuleComplete(id: Int?): Boolean {
        val h = hideoutModules?.values?.find { it?.id == id }
        return h?.complete == true
    }

    fun isHideoutObjectiveComplete(requirement: Hideout.Module.Require): Boolean {
        val h = hideoutObjectives?.values?.find { it?.id == requirement.id }
        return requirement.quantity == h?.progress
    }

    fun isQuestCompleted(quest: Quest): Boolean {
        val q = quests?.values?.find { it?.id == quest.id.toInt() }
        return q?.completed == true
    }

    fun isQuestCompleted(id: Int): Boolean {
        val q = quests?.values?.find { it?.id == id }
        return q?.completed == true
    }

    fun isObjectiveCompleted(objective: Quest.QuestObjective): Boolean {
        val o = questObjectives?.values?.find { it?.id == objective.id?.toInt() }
        return objective.number == o?.progress
    }

    fun getObjectiveProgress(objective: Quest.QuestObjective): Int {
        val o = questObjectives?.values?.find { it?.id == objective.id?.toInt() }
        return o?.progress ?: 0
    }

    fun hasKey(id: String): Boolean {
        return keysHave?.containsKey(id) == true
    }

    fun hasKey(item: Item): Boolean {
        return keysHave?.containsKey(item.id) == true
    }

    fun toggleKey(item: Item) {
        if (hasKey(item)) {
            userRefTracker("keysHave/${item.id}").removeValue()
        } else {
            userRefTracker("keysHave/${item.id}").setValue(true)
        }
    }
}
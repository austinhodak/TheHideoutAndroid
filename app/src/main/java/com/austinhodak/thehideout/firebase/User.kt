package com.austinhodak.thehideout.firebase

import com.austinhodak.tarkovapi.models.Hideout
import com.austinhodak.tarkovapi.room.models.Quest
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    var questObjectives: Map<String, UQuestObjective?>? = null,
    var quests: Map<String, UQuest?>? = null,
    var hideoutModules: Map<String, UHideout?>? = null,
    var hideoutObjectives: Map<String, UHideoutObjective?>? = null,
) {
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

    fun isHideoutModuleComplete(id: Int): Boolean {
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

    fun isObjectiveCompleted(objective: Quest.QuestObjective): Boolean {
        val o = questObjectives?.values?.find { it?.id == objective.id?.toInt() }
        return objective.number == o?.progress
    }
}
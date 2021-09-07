package com.austinhodak.thehideout.firebase

import com.austinhodak.tarkovapi.room.models.Quest
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class UserQuest(
    var questObjectives: Map<String, UQuestObjective?>? = null,
    var quests: Map<String, UQuest?>? = null
) {
    data class UQuestObjective(
        var progress: Int? = null,
        var id: Int? = null
    )

    data class UQuest(
        var id: Int? = null,
        var completed: Boolean? = null
    )

    fun isQuestCompleted(quest: Quest): Boolean {
        val q = quests?.values?.find { it?.id == quest.id.toInt() }
        return q?.completed == true
    }

    fun isObjectiveCompleted(objective: Quest.QuestObjective): Boolean {
        val o = questObjectives?.values?.find { it?.id == objective.id?.toInt() }
        return objective.number == o?.progress
    }
}
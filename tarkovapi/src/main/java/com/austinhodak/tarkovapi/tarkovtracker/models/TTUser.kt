package com.austinhodak.tarkovapi.tarkovtracker.models

import com.austinhodak.tarkovapi.tarkovtracker.TTRepository
import org.json.JSONObject

data class TTUser (
    val shareName: String,
    val hideoutObjectives: HashMap<String, TTObjective>,
    val gameEdition: Int,
    val objectives: HashMap<String, TTObjective>,
    val dataVersion: Int,
    val level: Int,
    val quests: HashMap<String, TTQuest>,
    val hideout: HashMap<String, TTQuest>,
    val self: Boolean,
    val hide: Boolean
) {
    data class TTQuest (
        val timeComplete: Long?,
        val complete: Boolean
    ) {
        fun toMap(id: String): Map<String, Any?> {
            return mapOf(
                "completed" to complete,
                "timeComplete" to timeComplete,
            )
        }
    }

    fun getQuest(id: String): TTQuest? {
        return quests[id]
    }

    fun getObjective(id: String): TTObjective? {
        return objectives[id]
    }

    fun getHideoutModule(id: String): TTQuest? {
        return hideout[id]
    }

    fun getHideoutObjective(id: String): TTObjective? {
        return hideoutObjectives[id]
    }

    fun isQuestComplete(id: String): Boolean {
        return quests[id]?.complete ?: false
    }

    fun isObjectiveComplete(id: String): Boolean {
        return objectives[id]?.complete ?: false
    }

    fun isHideoutComplete(id: String): Boolean {
        return hideout[id]?.complete ?: false
    }

    fun isHideoutObjectiveComplete(id: String): Boolean {
        return hideoutObjectives[id]?.complete ?: false
    }

    data class TTObjective (
        val timeComplete: Long?,
        val complete: Boolean,
        val have: Long?
    ) {
        fun toMap(id: String): Map<String, Any?> {
            return mapOf(
                "completed" to complete,
                "id" to id.toInt(),
                "timeComplete" to timeComplete,
                "progress" to have
            )
        }
    }
}

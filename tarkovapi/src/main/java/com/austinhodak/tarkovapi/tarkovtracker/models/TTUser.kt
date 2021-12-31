package com.austinhodak.tarkovapi.tarkovtracker.models

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
        val timeComplete: Long,
        val complete: Boolean
    ) {
        fun toMap(id: String): Map<String, Any?> {
            return mapOf(
                "completed" to complete,
                "id" to id.toInt(),
                "timeComplete" to timeComplete,
            )
        }
    }

    data class TTObjective (
        val timeComplete: Long,
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

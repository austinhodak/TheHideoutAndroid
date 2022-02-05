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
        var id: Int? = null,
        var completed: Boolean? = null
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
        var id: Int? = null,
        var completed: Boolean? = null
    )
}
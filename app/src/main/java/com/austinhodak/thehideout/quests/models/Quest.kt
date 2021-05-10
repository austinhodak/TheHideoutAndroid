package com.austinhodak.thehideout.quests.models

import android.util.Log
import androidx.annotation.DrawableRes
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.firebase.UserFB
import com.austinhodak.thehideout.quests.QuestsHelper
import com.austinhodak.thehideout.utils.userRef

data class Quest(
    var id: Int,
    var require: QuestRequire,
    var giver: String,
    var turnin: String,
    var title: String,
    var wiki: String,
    var exp: Int,
    //var unlocks: List<String>,
    var reputation: List<QuestReputation>,
    var objectives: List<QuestObjectives>
) {
    data class QuestRequire(
        var level: Int,
        var quest: List<String>
    )

    data class QuestReputation(
        var trader: String,
        var rep: Double
    )

    data class QuestObjectives(
        var type: String,
        var target: String,
        var number: Int,
        var location: String,
        var id: Int,
        var with: List<String>,
        var tool: String?,
        var hint: String?
    ) {

        fun isCompleted(objectives: UserFB.UserFBQuestObjectives?): Boolean {
            if (objectives == null) return false
            val pg = objectives.progress
            return if (pg?.containsKey("\"$id\"") == true) {
                return pg["\"$id\""] == number
            } else {
                false
            }
        }

        fun getCount(objectives: UserFB.UserFBQuestObjectives?): Int {
            if (objectives == null) return 0
            val pg = objectives.progress
            return if (pg?.containsKey("\"$id\"") == true) {
                return pg["\"$id\""] ?: 0
            } else {
                0
            }
        }

        override fun toString(): String {
            return when (type) {
                "kill" -> "Eliminate $number $target on $location"
                "collect" -> "Hand over ${getNumber()}$target"
                "pickup" -> "Pick-up ${getNumber()}$target"
                "key" -> "$target needed on $location"
                "place" -> "Place $target on $location"
                "mark" -> "Place MS2000 marker at $target on $location"
                "locate" -> "Location $target on $location"
                "find" -> "Find in raid $number $target"
                "reputation" -> "Reach loyalty level $number with $target"
                "warning" -> target
                "skill" -> "Reach skill level $number with $target"
                "survive" -> "Survive in the raid at $location $number times."
                else -> ""
            }
        }

        @DrawableRes fun getIcon(): Int {
            return when (type) {
                "kill" -> R.drawable.icons8_sniper_96
                "collect" -> R.drawable.ic_baseline_swap_horizontal_circle_24
                "pickup" -> R.drawable.icons8_upward_arrow_96
                "key" -> R.drawable.icons8_key_100
                "place" -> R.drawable.icons8_low_importance_96
                "mark" -> R.drawable.icons8_low_importance_96
                "locate" -> R.drawable.ic_baseline_location_searching_24
                "find" -> R.drawable.ic_baseline_location_searching_24
                "reputation" -> R.drawable.ic_baseline_thumb_up_24
                "warning" -> R.drawable.ic_baseline_warning_24
                "skill" -> R.drawable.ic_baseline_fitness_center_24
                else -> R.drawable.icons8_sniper_96
            }
        }

        fun getNumberString(): String {
            return if (number <= 1) "" else "${number}x "
        }

        private fun getNumber(): String {
            return if (number <= 1) "" else "$number "
        }

        fun increment(objectivesList: UserFB.UserFBQuestObjectives) {
            if (getCount(objectivesList) < number) {
                userRef("/questObjectives/progress/\"$id\"").setValue(getCount(objectivesList) + 1)
            }
        }

        fun decrement(objectivesList: UserFB.UserFBQuestObjectives) {
            if (getCount(objectivesList) in 1..number) {
                if (getCount(objectivesList) - 1 == 0) {
                    userRef("/questObjectives/progress/\"$id\"").removeValue()
                } else {
                    userRef("/questObjectives/progress/\"$id\"").setValue(getCount(objectivesList) - 1)
                }
            }
        }
    }

    fun getLocation(): String {
        val string: ArrayList<String> = ArrayList()

        if (!objectives.isNullOrEmpty()) {
            for (objective in objectives) {
                if (!string.contains(objective.location)) {
                    string.add(objective.location)
                }
            }
        }

        return string.joinToString(", ")
    }

    fun isCompleted(quests: UserFB.UserFBQuests): Boolean {
        Log.d("QUESTS_COMPLETED", quests.toString())
        if (quests.completed == null) return false
        for (q in quests.completed!!) {
            if ("\"$id\"" == q.key) {
                return true
            }
        }
        return false
    }

    fun isLocked(quests: UserFB.UserFBQuests): Boolean {
        return QuestsHelper.getLockedQuests(quests).contains(this)
    }
}

enum class Traders (var id: String) {
    PRAPOR      ("Prapor"),
    THERAPIST   ("Therapist"),
    FENCE       ("Fence"),
    SKIER       ("Skier"),
    PEACEKEEPER ("Peacekeeper"),
    MECHANIC    ("Mechanic"),
    RAGMAN      ("Ragman"),
    JAEGER      ("Jaeger"),
}

enum class Maps (var id: String) {
    ANY         ("Any"),
    FACTORY     ("Factory"),
    CUSTOMS     ("Customs"),
    WOODS       ("Woods"),
    SHORELINE   ("Shoreline"),
    INTERCHANGE ("Interchange"),
    RESERVE     ("Reserve"),
    THELAB        ("Labs")
}
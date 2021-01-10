package com.austinhodak.thehideout.quests.models

import androidx.annotation.DrawableRes
import com.austinhodak.thehideout.R

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
        var id: Int
    ) {
        override fun toString(): String {
            return when (type) {
                "kill" -> "Eliminate $number $target on $location"
                "collect" -> "Hand over ${getNumber()} $target"
                "pickup" -> "Pick-up ${getNumber()} $target"
                "key" -> "$target needed on $location"
                "place" -> "Place $target on $location"
                "mark" -> "Place MS2000 marker at $target on $location"
                "locate" -> "Location $target on $location"
                "find" -> "Find in raid $number $target"
                "reputation" -> "Reach loyalty level $number with $target"
                "warning" -> target
                "skill" -> "Reach skill level $number with $target"
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

        private fun getNumber(): String {
            return if (number <= 1) "" else number.toString()
        }
    }
}

enum class Traders (var id: String) {
    PRAPOR      ("Prapor"),
    THERAPIST   ("Therapist"),
    SKIER       ("Skier"),
    PEACEKEEPER ("Peacekeeper"),
    MECHANIC    ("Mechanic"),
    RAGMAN      ("Ragman"),
    JAEGER      ("Jaeger"),
    FENCE       ("Fence"),
}
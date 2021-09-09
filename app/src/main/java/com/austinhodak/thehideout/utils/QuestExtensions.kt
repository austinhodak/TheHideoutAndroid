package com.austinhodak.thehideout.utils

import androidx.annotation.DrawableRes
import com.austinhodak.tarkovapi.room.enums.Traders
import com.austinhodak.tarkovapi.room.models.Quest
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.firebase.User

@DrawableRes
fun Quest.QuestObjective.getIcon(): Int {
    return when (type) {
        "kill" -> R.drawable.icons8_sniper_96
        "collect" -> R.drawable.ic_baseline_swap_horizontal_circle_24
        "pickup" -> R.drawable.icons8_upward_arrow_96
        "key" -> R.drawable.icons8_key_100
        "place" -> R.drawable.icons8_low_importance_96
        "mark" -> R.drawable.icons8_low_importance_96
        "locate" -> R.drawable.ic_baseline_location_searching_24
        "find" -> R.drawable.ic_baseline_check_circle_outline_24
        "reputation" -> R.drawable.ic_baseline_thumb_up_24
        "warning" -> R.drawable.ic_baseline_warning_24
        "skill" -> R.drawable.ic_baseline_fitness_center_24
        "build" -> R.drawable.ic_baseline_build_circle_24
        else -> R.drawable.icons8_sniper_96
    }
}

fun Quest.isLocked(userData: User?): Boolean {
    val completedQuests = userData?.quests?.values?.filter { it?.completed == true }?.map { it?.id }
    return if (userData?.isQuestCompleted(this) == true) {
        false
    } else {
        if (requirement?.quests.isNullOrEmpty() && requirement?.level ?: 0 <= 15) {
            false
        } else {
            requirement?.quests?.forEach {
                if (it != null)
                    return completedQuests?.containsAll(it) == false
            }
            true
        }
    }
}

fun Quest.isAvailable(userData: User?): Boolean {
    val completedQuests = userData?.quests?.values?.filter { it?.completed == true }?.map { it?.id }
    return if (userData?.isQuestCompleted(this) == true) {
        false
    } else {
        if (requirement?.quests.isNullOrEmpty() && requirement?.level ?: 0 <= 15) {
            true
        } else {
            requirement?.quests?.forEach {
                if (it != null)
                    return completedQuests?.containsAll(it) == true
            }
            false
        }
    }
}

fun Quest.trader(): Traders {
    return Traders.valueOf(giver?.name?.toUpperCase()!!)
}
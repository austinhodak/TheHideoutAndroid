package com.austinhodak.thehideout.utils

import androidx.annotation.DrawableRes
import com.austinhodak.tarkovapi.room.enums.Traders
import com.austinhodak.tarkovapi.room.models.Quest
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.firebase.User
import com.google.firebase.database.ServerValue

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
        if (requirement?.level ?: 0 > userData?.playerLevel ?: 71) return true
        if (requirement?.quests.isNullOrEmpty()) {
            false
        } else {
            requirement?.quests?.any { l1 ->
                l1?.all {
                    completedQuests?.contains(it) == false
                } == true
            } == true
        }
    }
}

fun Quest.isAvailable(userData: User?): Boolean {
    val completedQuests = userData?.quests?.values?.filter { it?.completed == true }?.map { it?.id }
    if (this.isLocked(userData)) return false
    return if (userData?.isQuestCompleted(this) == true) {
        false
    } else {
        if (requirement?.quests.isNullOrEmpty()) {
            true
        } else {
            if (userData?.playerLevel ?: 71 >= requirement?.level ?: 0) {
                requirement?.quests?.all { l1 ->
                    l1?.any {
                        completedQuests?.contains(it) == true
                    } == true
                } == true
            } else {
                false
            }
        }
    }
}

fun Quest.trader(): Traders {
    return Traders.valueOf(giver?.name?.uppercase()!!)
}

fun Quest.QuestObjective.increment() {
    userRefTracker("questObjectives/${this.id?.addQuotes()}/id").setValue(this.id?.toInt())
    userRefTracker("questObjectives/${this.id?.addQuotes()}/progress").setValue(
        ServerValue.increment(
            1
        )
    )
}

fun Quest.QuestObjective.decrement() {
    userRefTracker("questObjectives/${this.id?.addQuotes()}/id").setValue(this.id?.toInt())
    userRefTracker("questObjectives/${this.id?.addQuotes()}/progress").setValue(
        ServerValue.increment(
            -1
        )
    )
}


fun Quest.completed() {
    val quest = this
    log("quest_completed", quest.id, quest.title.toString(), "quest")
    userRefTracker("quests/${quest.id.addQuotes()}").setValue(
        mapOf(
            "id" to quest.id.toInt(),
            "completed" to true
        )
    )

    //Mark quest objectives completed
    for (obj in quest.objective!!) {
        obj.completed()
    }
}

fun Quest.QuestObjective.completed() {
    val objective = this
    log("objective_complete", objective.toString(), objective.toString(), "quest_objective")
    userRefTracker("questObjectives/${objective.id?.toInt()?.addQuotes()}").setValue(
        mapOf(
            "id" to objective.id?.toInt(),
            "progress" to objective.number,
            "completed" to true
        )
    )
    objective.target?.first()?.let {
        when (objective.type) {
            "collect", "find", "key", "build" -> {
                userRefTracker("items/${it}/questObjective/${objective.id?.addQuotes()}").removeValue()
            }
            else -> {}
        }
    }
}

fun Quest.QuestObjective.undo() {
    val objective = this
    log("objective_un_complete", objective.toString(), objective.toString(), "quest_objective")
    userRefTracker("questObjectives/${objective.id?.toInt()?.addQuotes()}").removeValue()
}

fun Quest.undo(objectives: Boolean = false) {
    val quest = this
    log("quest_undo", quest.id, quest.title.toString(), "quest")

    if (objectives) {
        for (obj in quest.objective!!) {
            obj.undo()
        }
    }

    userRefTracker("quests/${quest.id.addQuotes()}").setValue(
        mapOf(
            "id" to quest.id.toInt(),
            "completed" to false
        )
    )
}

fun User.toggleObjective(quest: Quest, objective: Quest.QuestObjective) {
    if (isObjectiveCompleted(objective)) {
        objective.undo()
        quest.undo()
    } else {
        objective.completed()
    }
}
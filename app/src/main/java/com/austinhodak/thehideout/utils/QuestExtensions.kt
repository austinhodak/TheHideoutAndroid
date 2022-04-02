package com.austinhodak.thehideout.utils

import androidx.annotation.DrawableRes
import com.austinhodak.tarkovapi.room.enums.Traders
import com.austinhodak.tarkovapi.room.models.Quest
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.firebase.FSUser
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import timber.log.Timber

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

fun Quest.isLocked(userData: FSUser?): Boolean {
    val completedQuests = userData?.progress?.getCompletedQuestIDs()

    return if (userData?.progress?.isQuestCompleted(this) == true) {
        false
    } else {
        if (requirement?.level ?: 0 > userData?.playerLevel ?: 71) return true
        if (requirement?.quests.isNullOrEmpty()) {
            false
        } else {
            requirement?.quests?.any { l1 ->
                l1?.all {
                    completedQuests?.contains(it.toString()) == false
                } == true
            } == true
        }
    }
}

fun Quest.isAvailable(userData: FSUser?): Boolean {
    val completedQuests = userData?.progress?.getCompletedQuestIDs()
    if (this.isLocked(userData)) return false
    return if (userData?.progress?.isQuestCompleted(this) == true) {
        false
    } else {
        if (requirement?.quests.isNullOrEmpty()) {
            true
        } else {
            if (userData?.playerLevel ?: 71 >= requirement?.level ?: 0) {
                requirement?.quests?.all { l1 ->
                    l1?.any {
                        completedQuests?.contains(it.toString()) == true
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
    userFirestore()?.update(
        "progress.questObjectives.${this.id}.progress", FieldValue.increment(1)
    )
}

fun Quest.QuestObjective.decrement() {
    userFirestore()?.update(
        "progress.questObjectives.${this.id}.progress", FieldValue.increment(-1)
    )
}


fun Quest.completed(success: ((Boolean) -> Unit)? = null) {
    Timber.d("CLICKING COMPLETED")
    val quest = this
    log("quest_completed", quest.id, quest.title.toString(), "quest")

    val objectiveMap = quest.objective!!.associateBy({it.id}, {
        mapOf(
            "completed" to true,
            "timestamp" to Timestamp.now()
        )
    })

    userFirestore()?.set(
        hashMapOf(
            "progress" to hashMapOf(
                "quests" to hashMapOf(
                    quest.id to hashMapOf(
                        "completed" to true,
                        "timestamp" to Timestamp.now()
                    )
                ),
                "questObjectives" to objectiveMap
            )
        ),
        SetOptions.merge()
    )?.addOnCompleteListener {
        success?.invoke(it.isSuccessful)
    }?.addOnCompleteListener {
        if (!it.isSuccessful)
            Timber.e(it.exception)
    }
}

fun Quest.QuestObjective.completed(success: ((Boolean) -> Unit)? = null) {
    val objective = this
    log("objective_complete", objective.toString(), objective.toString(), "quest_objective")

    userFirestore()?.set(
        hashMapOf(
            "progress" to hashMapOf(
                "questObjectives" to hashMapOf(
                    objective.id to hashMapOf(
                        "completed" to true,
                        "timestamp" to Timestamp.now(),
                        "progress" to this.number
                    )
                )
            )
        ),
        SetOptions.merge()
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

    userFirestore()?.set(
        hashMapOf(
            "progress" to hashMapOf(
                "questObjectives" to hashMapOf(
                    objective.id to FieldValue.delete()
                )
            )
        ),
        SetOptions.merge()
    )
}

fun Quest.undo(objectives: Boolean = false) {
    val quest = this
    log("quest_undo", quest.id, quest.title.toString(), "quest")

    if (objectives) {
        for (obj in quest.objective!!) {
            obj.undo()
        }
    }

    userFirestore()?.set(
        hashMapOf(
            "progress" to hashMapOf(
                "quests" to hashMapOf(
                    quest.id to FieldValue.delete()
                )
            )
        ),
        SetOptions.merge()
    )
}

fun FSUser.toggleObjective(quest: Quest, objective: Quest.QuestObjective) {
    if (this.progress?.isQuestObjectiveCompleted(objective) == true) {
        objective.undo()
        quest.undo()
    } else {
        objective.completed()
    }
}
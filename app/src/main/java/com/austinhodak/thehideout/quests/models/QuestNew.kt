package com.austinhodak.thehideout.quests.models

import androidx.annotation.DrawableRes
import com.austinhodak.thehideout.R

data class QuestNew(
    val exp: Int,
    val giver: Giver,
    val id: String,
    val objectives: List<Objective>,
    val reputation: List<Reputation>,
    val requirements: Requirements,
    val title: String,
    val turnin: Turnin,
    val unlocks: List<String>,
    val wikiLink: String
) {
    data class Giver(
        val id: String,
        val name: String
    )

    data class Objective(
        val id: String,
        val location: String,
        val number: Int,
        val target: String,
        val targetItem: TargetItem?,
        val type: String
    ) {
        data class TargetItem(
            val id: String,
            val name: String
        )
    }

    data class Reputation(
        val amount: Double,
        val trader: Trader
    ) {
        data class Trader(
            val id: String,
            val name: String
        )
    }

    data class Requirements(
        val quests: List<String>
    )

    data class Turnin(
        val id: String,
        val name: String
    )

    fun needsItem(id: String): Boolean {
        return objectives.any { it.targetItem?.id == id }
    }
    
    @DrawableRes
    fun getTraderIcon(): Int {
        return when (giver.name) {
            "Prapor" -> R.drawable.prapor_portrait
            "Therapist" -> R.drawable.therapist_portrait
            "Fence" -> R.drawable.fence_portrait
            "Skier" -> R.drawable.skier_portrait
            "Peacekeeper" -> R.drawable.peacekeeper_portrait
            "Mechanic" -> R.drawable.mechanic_portrait
            "Ragman" -> R.drawable.ragman_portrait
            "Jaeger" -> R.drawable.jaeger_portrait
            else -> R.drawable.jaeger_portrait
        }
    }
}
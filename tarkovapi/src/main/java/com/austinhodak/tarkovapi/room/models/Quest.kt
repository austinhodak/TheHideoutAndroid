package com.austinhodak.tarkovapi.room.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.austinhodak.tarkovapi.fragment.TraderFragment

@Entity(tableName = "quests")
data class Quest(
    @PrimaryKey val id: String,
    val title: String? = null,
    val wikiLink: String? = null,
    val exp: Int? = null,
    val giver: TraderFragment? = null,
    val turnin: TraderFragment? = null,
    val unlocks: List<String?>? = null,
    //@Embedded(prefix = "requirement_") val requirement: QuestRequirement? = null,
    val requirement: QuestRequirement? = null,
    //val reputation: List<RepFragment>? = null,
    val objective: List<QuestObjective>? = null,
) {

    data class QuestRequirement(
        val level: Int? = null,
        val quests: List<List<Int?>?>? = null,
        val prerequisiteQuests: List<List<Quest?>?>? = null
    )

    data class QuestObjective(
        val id: String? = null,
        val type: String? = null,
        val target: List<String>? = null,
        val number: Int? = null,
        val location: String? = null,
        val targetItem: Pricing? = null
    )

    fun getObjective(itemID: String? = null): QuestObjective? {
        return objective?.find { it.targetItem?.id == itemID }
    }

    fun getMaps(): String {
        val list = objective?.map { it.location.toString() }?.distinct()
        return list?.joinToString(separator = ", ") { it } ?: ""
    }
}
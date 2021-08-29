package com.austinhodak.tarkovapi.room.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.austinhodak.thehideout.fragment.ObjectiveFragment
import com.austinhodak.thehideout.fragment.QuestFragment
import com.austinhodak.thehideout.fragment.RepFragment
import com.austinhodak.thehideout.fragment.TraderFragment

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
    val requirement: QuestFragment.Requirements? = null,
    val reputation: List<RepFragment>? = null,
    val objective: List<ObjectiveFragment>? = null,
) {

    data class QuestRequirement(
        val level: Int? = null,
        val quests: List<List<Int?>?>? = null,
        val prerequisiteQuests: List<List<Quest?>?>? = null
    )

    fun getObjective(itemID: String? = null): ObjectiveFragment? {
        return objective?.find { it.targetItem?.fragments?.itemFragment?.id == itemID }
    }

    fun getMaps(): String {
        val list = objective?.map { it.location.toString() }?.distinct()
        return list?.joinToString(separator = ", ") { it } ?: ""
    }
}
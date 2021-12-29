package com.austinhodak.tarkovapi.room.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.austinhodak.tarkovapi.fragment.TraderFragment
import com.austinhodak.tarkovapi.room.enums.Traders
import com.austinhodak.tarkovapi.utils.Maps
import timber.log.Timber

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

    fun getGiverName(): String? = giver?.name

    data class QuestRequirement(
        val level: Int? = null,
        val quests: List<List<Int?>?>? = null,
        val prerequisiteQuests: List<List<Quest?>?>? = null
    )

    fun requiredQuestsList(): List<Int?>? {
        return requirement?.quests?.flatMap { it.orEmpty() }
    }

    data class QuestObjective(
        val id: String? = null,
        val type: String? = null,
        val target: List<String>? = null,
        val number: Int? = null,
        val location: String? = null,
        val targetItem: Pricing? = null
    ) {
        override fun toString(): String {
            val location = com.austinhodak.tarkovapi.room.enums.Maps.values().find { it.int == location?.toInt() }?.id
            return when (type) {
                "kill" -> "Eliminate $number ${target?.first()} on $location"
                "collect" -> "Hand over ${getNumber()}${target?.first()}"
                "pickup" -> "Pick-up ${getNumber()}${target?.first()}"
                "key" -> "${target?.first()} needed on $location"
                "place" -> "Place ${target?.first()} on $location"
                "mark" -> "Place MS2000 marker at ${target?.first()} on $location"
                "locate" -> "Locate ${target?.first()} on $location"
                "find" -> "Find in raid $number ${target?.first()}"
                "reputation" -> "Reach loyalty level $number with ${target?.first()}"
                "warning" -> target?.first() ?: ""
                "skill" -> "Reach skill level $number with ${target?.first()}"
                "survive" -> "Survive in the raid at $location $number times."
                else -> ""
            }
        }

        fun toStringBasic(): String {
            val location = com.austinhodak.tarkovapi.room.enums.Maps.values().find { it.int == location?.toInt() }?.id
            return when (type) {
                "kill" -> "${getNumber()} ${target?.first()} on $location"
                "collect" -> "${getNumber()}${target?.first()}"
                "pickup" -> "${getNumber()}${target?.first()} on $location"
                "key" -> "${target?.first()} needed on $location"
                "place" -> "${target?.first()} on $location"
                "mark" -> "MS2000 marker at ${target?.first()} on $location"
                "locate" -> "${target?.first()} on $location"
                "find" -> "$number ${target?.first()}"
                "reputation" -> "Loyalty Level $number with ${Traders.values().find { it.int == target?.first()?.toInt() ?: 0}?.id}"
                "warning" -> target?.first() ?: ""
                "skill" -> "$number with ${target?.first()}"
                "survive" -> "At $location $number times."
                else -> ""
            }
        }

        fun getNumberString(): String {
            return if (number ?: 0 <= 1) "" else "${number}x "
        }

        private fun getNumber(): String {
            return if (number ?: 0 <= 1) "" else "$number "
        }

        fun isFIR(): Boolean = type == "find"
    }

    fun getObjective(itemID: String? = null): QuestObjective? {
        return objective?.find { it.targetItem?.id == itemID }
    }

    fun getMaps(mapsList: Maps): String {
        val list = objective?.map {  it.location?.toInt() }?.distinct()
        val mapList = list?.map { mapsList.getMap(it) }
        Timber.d(list.toString())
        Timber.d(mapList.toString())
        return mapList?.joinToString(separator = ", ") { it?.locale?.en ?: "Any Map" } ?: ""
    }

    fun getMapsIDs(): List<Int?>? = objective?.map { it.location?.toInt() }?.distinct()
}
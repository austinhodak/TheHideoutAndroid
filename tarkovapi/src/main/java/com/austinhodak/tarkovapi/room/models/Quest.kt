package com.austinhodak.tarkovapi.room.models

import androidx.lifecycle.MutableLiveData
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.austinhodak.tarkovapi.fragment.ObjectiveFragment
import com.austinhodak.tarkovapi.fragment.RepFragment
import com.austinhodak.tarkovapi.fragment.TraderFragment
import com.austinhodak.tarkovapi.room.TarkovDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

@Entity(tableName = "quests")
data class Quest(
    @PrimaryKey val id: String,
    val title: String? = null,
    val wikiLink: String? = null,
    val exp: Int? = null,
    val giver: TraderFragment? = null,
    val turnin: TraderFragment? = null,
    val unlocks: List<String?>? = null,
    @Embedded(prefix = "requirement_") val requirement: QuestRequirement? = null,
    val reputation: List<RepFragment>? = null,
    val objective: List<ObjectiveFragment>? = null,
) {

    data class QuestRequirement(
        val level: Int? = null,
        val quests: List<List<Int?>?>
    )

    fun isRequiredForKappa(
        database: TarkovDatabase
    ): MutableLiveData<Boolean> = runBlocking(Dispatchers.IO) {
        val result: List<Int?> = database.QuestDao().getAlLQuests().flatMap { quest -> quest.requirement?.quests?.flatMap { it!! }!! }
        MutableLiveData<Boolean>(result.filterNotNull().contains(id.toInt()))
    }

    fun getObjective(itemID: String? = null): ObjectiveFragment? {
        return objective?.find { it.targetItem?.fragments?.itemFragment?.id == itemID }
    }

    fun getMaps(): String {
        val list = objective?.map { it.location.toString() }?.distinct()
        return list?.joinToString(separator = ", ") { it } ?: ""
    }
}
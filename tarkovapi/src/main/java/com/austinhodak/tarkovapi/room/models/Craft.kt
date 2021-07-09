package com.austinhodak.tarkovapi.room.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.austinhodak.tarkovapi.fragment.TaskItem
import kotlin.math.roundToInt

@Entity(tableName = "crafts")
data class Craft(
    @PrimaryKey val id: Int,
    val source: String,
    val duration: Int,
    val requiredItems: List<TaskItem>?,
    val rewardItems: List<TaskItem>?,
) {
    fun totalCost(): Int {
        return requiredItems?.sumBy { (it.count * (it.item.fragments.itemFragment.avg24hPrice ?: 0)).roundToInt() } ?: 0
    }

    fun estimatedProfit(): Int? {
        return rewardItems?.first()?.item?.fragments?.itemFragment?.avg24hPrice?.times(rewardItems.first().count)?.minus(totalCost())?.roundToInt()
    }
    
    fun estimatedProfitPerHour(): Int? = estimatedProfit()?.div((duration / 3600.0))?.roundToInt()
}
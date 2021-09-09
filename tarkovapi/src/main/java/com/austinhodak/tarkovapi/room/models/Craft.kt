package com.austinhodak.tarkovapi.room.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.austinhodak.tarkovapi.models.Hideout
import kotlin.math.roundToInt

@Entity(tableName = "crafts")
data class Craft(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val duration: Int? = null,
    val requiredItems: List<CraftItem?>? = null,
    val rewardItems: List<CraftItem?>? = null,
    val source: String? = null
) {
    data class CraftItem(
        val count: Int? = null,
        val item: Pricing? = null
    ) {

    }

    fun getSourceID(hideout: Hideout?): Int? {
        if (hideout == null) return null
        val station = source?.split(" level ")?.get(0)?.lowercase()
        val level = source?.split(" level ")?.get(1)?.toInt()
        return hideout.modules?.find { it?.level == level && it?.module?.lowercase() == station }?.id
    }

    fun totalCost(): Int {
        return requiredItems?.sumBy { (it?.count!!.times((it.item?.avg24hPrice ?: 0))) } ?: 0
    }

    fun estimatedProfit(): Int? {
        return rewardItems?.first()?.item?.avg24hPrice?.minus(totalCost())
    }

    fun estimatedProfitPerHour(): Int? = estimatedProfit()?.div((duration?.div(3600.0)!!))?.roundToInt()
}
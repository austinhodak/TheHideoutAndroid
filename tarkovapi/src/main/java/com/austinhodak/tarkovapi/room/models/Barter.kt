package com.austinhodak.tarkovapi.room.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "barters")
data class Barter(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val requiredItems: List<Craft.CraftItem?>? = null,
    val rewardItems: List<Craft.CraftItem?>? = null,
    val source: String? = null
) {
    fun totalCost(): Int {
        return requiredItems?.sumOf { (it?.count!!.times((it.item?.getCheapestBuyRequirements()?.price ?: 0))) } ?: 0
    }

    fun estimatedProfit(): Int? {
        return rewardItems?.first()?.item?.getHighestSell()?.price?.minus(totalCost())
    }
}
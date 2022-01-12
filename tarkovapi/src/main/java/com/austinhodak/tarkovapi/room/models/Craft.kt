package com.austinhodak.tarkovapi.room.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.austinhodak.tarkovapi.models.Hideout
import com.austinhodak.tarkovapi.utils.euroToRouble
import com.austinhodak.tarkovapi.utils.fromDtoR
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

    fun rewardItem(): CraftItem? = rewardItems?.first()

    fun rewardQuantity(): Int = rewardItems?.first()?.count ?: 1

    fun getSourceID(hideout: Hideout?): Int? {
        if (hideout == null) return null
        val station = source?.split(" level ")?.get(0)?.lowercase()
        val level = source?.split(" level ")?.get(1)?.toInt()
        return hideout.modules?.find { it?.level == level && it?.module?.lowercase() == station }?.id
    }

    fun totalCost(): Int {
        return requiredItems?.sumOf {
            val cheapestBuy = it?.item?.getCheapestBuyRequirements()
            val price = if (cheapestBuy?.currency == "USD") {
                cheapestBuy.price?.fromDtoR()?.roundToInt()
            } else if (cheapestBuy?.currency == "EUR") {
                euroToRouble(cheapestBuy.price?.toLong()).toInt()
            } else {
                cheapestBuy?.price
            }

            (it?.count!!.times((price ?: 0)))
        } ?: 0
    }

    fun estimatedProfit(): Int? {
        val price = rewardItem()?.item?.getHighestSell()?.price?.times(rewardQuantity())
        return price?.minus(totalCost())
    }

    fun estimatedProfitPerHour(): Int? = estimatedProfit()?.div((duration?.div(3600.0)!!))?.roundToInt()

    fun getCraftingTime(): String {
        val duration = duration ?: 1
        val hours = duration.div(3600)
        val minutes = (duration % 3600) / 60
        val seconds = duration % 60

        return String.format("%02dH:%02dM", hours, minutes)
    }
}
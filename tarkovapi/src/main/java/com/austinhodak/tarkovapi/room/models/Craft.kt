package com.austinhodak.tarkovapi.room.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.apollographql.apollo3.mpp.currentTimeMillis
import com.austinhodak.tarkovapi.fragment.TaskItem
import com.austinhodak.tarkovapi.models.Hideout
import com.austinhodak.tarkovapi.utils.euroToRouble
import com.austinhodak.tarkovapi.utils.fromDtoR
import java.io.Serializable
import java.text.SimpleDateFormat
import kotlin.math.roundToInt

@Entity(tableName = "crafts")
data class Craft(
    @PrimaryKey val id: String,
    val duration: Int? = null,
    val requiredItems: List<CraftItem?>? = null,
    val rewardItems: List<CraftItem?>? = null,
    val source: String? = null
) : Serializable {
    data class CraftItem(
        val count: Int? = null,
        val item: Pricing? = null,
        val attributes: List<Attributes?>? = null
    ) : Serializable {
        fun isTool(): Boolean = attributes?.any { it?.name == "tool" && it.value == "true" } == true

        data class Attributes(
            val type: String? = null,
            val name: String? = null,
            val value: String? = null
        ) : Serializable
    }

    fun getSourceName(): String? {
        return source?.split(" level ")?.get(0)
    }

    fun getSourceLevel(): Int? {
        return source?.split(" level ")?.get(1)?.toIntOrNull()
    }

    fun rewardItem(): CraftItem? = rewardItems?.first()

    fun rewardQuantity(): Int = rewardItems?.first()?.count ?: 1

    fun getSourceID(hideout: Hideout?): Int? {
        if (hideout == null) return null
        if (source == "Booze Generator") return hideout.modules?.find { it?.module == "Booze generator" }?.id
        if (source == "Bitcoin Farm") return hideout.modules?.find { it?.module == "Bitcoin farm" }?.id
        val station = source?.split(" level ")?.get(0)?.lowercase()
        val level = source?.split(" level ")?.get(1)?.toInt()
        return hideout.modules?.find { it?.level == level && it?.module?.lowercase() == station }?.id
    }

    fun totalCost(): Int {
        return requiredItems?.filterNot { it?.isTool() == true }?.sumOf {
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

    fun getCraftingTime(format: String = "%02dH:%02dM"): String {
        val duration = duration ?: 1
        val hours = duration.div(3600)
        val minutes = (duration % 3600) / 60
        val seconds = duration % 60

        return String.format(format, hours, minutes)
    }

    fun getFleaThroughput(): Int? {
        return rewardItem()?.item?.getHighestSell()?.price?.div((duration?.div(3600.0)!!))?.roundToInt()
    }

    fun getFinishTime(): String {
        val ms = duration?.times(1000) ?: 0
        val finishTime = currentTimeMillis() + ms
        val simpleDateFormat = SimpleDateFormat("EEE '@' hh:mm")
        return simpleDateFormat.format(finishTime)
    }
}
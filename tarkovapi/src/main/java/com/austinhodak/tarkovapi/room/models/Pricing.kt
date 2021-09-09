package com.austinhodak.tarkovapi.room.models

import com.austinhodak.tarkovapi.room.enums.ItemTypes
import com.austinhodak.tarkovapi.utils.getTraderLevel
import com.austinhodak.tarkovapi.utils.sourceTitle

data class Pricing(
    val id: String,
    val name: String?,
    val shortName: String?,
    val iconLink: String?,
    val imageLink: String?,
    val gridImageLink: String?,
    val avg24hPrice: Int?,
    val basePrice: Int,
    val lastLowPrice: Int?,
    val changeLast48h: Double?,
    val low24hPrice: Int?,
    val high24hPrice: Int?,
    val updated: String?,
    val types: List<ItemTypes?>,
    val width: Int?,
    val height: Int?,
    val sellFor: List<BuySellPrice>?,
    val buyFor: List<BuySellPrice>?
) {

    fun getPrice(): Int {
        return if (avg24hPrice ?: 0 > 0) {
            avg24hPrice ?: lastLowPrice ?: basePrice
        } else {
            lastLowPrice ?: basePrice
        }
    }

    data class BuySellPrice(
        val source: String?,
        val price: Int?,
        val requirements: List<Requirement>
    ) {
        data class Requirement(
            val type: String,
            val value: Int
        )

        fun getTitle(): String {
            return if (source == "fleaMarket") {
                "Flea Market"
            } else {
                if (requirements.isNotEmpty() && requirements.first().type == "loyaltyLevel") {
                    "${source?.sourceTitle()} ${requirements.first().value.getTraderLevel()}"
                } else {
                    source?.sourceTitle() ?: ""
                }
            }
        }

        fun isFleaMarket(): Boolean = source == "fleaMarket"
    }

    fun getFleaMarketBuy(): BuySellPrice? {
        return buyFor?.find { it.isFleaMarket() }
    }

    fun getHighestTraderSell(): BuySellPrice? {
        return sellFor?.filter { !it.isFleaMarket() }?.maxByOrNull {
            it.price ?: 0
        }
    }

    fun getInstaProfit(): Int? {
        return getHighestTraderSell()?.price?.minus(getFleaMarketBuy()?.price ?: lastLowPrice ?: 0)
    }
}
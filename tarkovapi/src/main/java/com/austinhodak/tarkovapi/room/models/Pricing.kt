package com.austinhodak.tarkovapi.room.models

import com.austinhodak.tarkovapi.room.enums.ItemType
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
    val types: List<ItemType?>,
    val width: Int?,
    val height: Int?,
    val sellFor: List<BuySellPrice>?,
    val buyFor: List<BuySellPrice>?
) {

    fun getPrice(): Int {
        return lastLowPrice ?: basePrice
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
    }
}
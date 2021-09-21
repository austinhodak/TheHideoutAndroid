package com.austinhodak.tarkovapi.room.models

import com.austinhodak.tarkovapi.room.enums.ItemTypes
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.tarkovapi.utils.getTraderLevel
import com.austinhodak.tarkovapi.utils.sourceTitle
import java.io.Serializable
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

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
    val buyFor: List<BuySellPrice>?,
    val wikiLink: String?
) : Serializable {

    fun getCheapestBuy(): BuySellPrice? {
        return buyFor?.minByOrNull { it.price ?: Int.MAX_VALUE }!!
    }

    fun getHighestSell(): BuySellPrice? {
        return sellFor?.maxByOrNull { it.price ?: Int.MIN_VALUE }!!
    }

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
    ) : Serializable {
        data class Requirement(
            val type: String,
            val value: Int
        ) : Serializable

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

    private fun getFleaMarketBuy(): BuySellPrice? {
        return buyFor?.find { it.isFleaMarket() }
    }

    private fun getHighestTraderSell(): BuySellPrice? {
        return sellFor?.filter { !it.isFleaMarket() }?.maxByOrNull {
            it.price ?: 0
        }
    }

    fun getInstaProfit(): Int? {
        return getHighestTraderSell()?.price?.minus(getFleaMarketBuy()?.price ?: lastLowPrice ?: 0)
    }

    fun getTotalCostWithExplanation(quantity: Int): String {
        if (shortName == "RUB" || shortName == "USD" || shortName == "EUR") {
            return quantity.asCurrency(shortName[0].toString())
        }

        if (quantity == 1) {
            avg24hPrice?.asCurrency()
        }

        val totalCost = avg24hPrice?.let {
            it * quantity
        }

        return "$quantity x ${avg24hPrice?.asCurrency()} = ${totalCost?.asCurrency()}"
    }

    fun calculateTax(salePrice: Long = avg24hPrice?.toLong() ?: (0).toLong()): Int {
        val mVO = basePrice.toDouble()
        val mVR = salePrice.toDouble()
        val mTi = 0.05
        val mTr = 0.05
        val mQ = 1
        val mPO = log10((mVO / mVR))
        val mPR = log10((mVR / mVO))

        val mPO4 = if (mVO > mVR) {
            Math.pow(4.0, mPO.pow(1.08))
        } else {
            Math.pow(4.0, mPO)
        }

        val mPR4 = if (mVR > mVO) {
            Math.pow(4.0, mPR.pow(1.08))
        } else {
            Math.pow(4.0, mPR)
        }

        val tax = (mVO * mTi * mPO4 * mQ + mVR * mTr * mPR4 * mQ).roundToInt()
        return tax
    }
}
package com.austinhodak.tarkovapi.room.models

import android.text.format.DateUtils
import com.apollographql.apollo3.mpp.currentTimeMillis
import com.austinhodak.tarkovapi.UserSettingsModel
import com.austinhodak.tarkovapi.fragment.ItemFragment
import com.austinhodak.tarkovapi.room.enums.ItemTypes
import com.austinhodak.tarkovapi.type.ItemType
import com.austinhodak.tarkovapi.utils.*
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

data class Pricing(
    val id: String,
    val name: String?,
    val shortName: String?,
    val iconLink: String? = "https://tarkov.dev/images/unknown-item-icon.jpg",
    val imageLink: String?,
    val gridImageLink: String? = "https://tarkov.dev/images/unknown-item-icon.jpg",
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
    val buyFor: List<BuySellPrice>?,
    val wikiLink: String?,
    val noFlea: Boolean?,
    val containsItem: List<Contains>?
) : Serializable {

    fun isDisabled(): Boolean = false
    fun hasChild(): Boolean = containsItem?.isNotEmpty() == true

    fun getIcon(): String = gridImageLink ?: iconLink ?: "https://tarkov.dev/images/unknown-item-icon.jpg"
    fun getCleanIcon(): String = iconLink ?: gridImageLink ?: "https://tarkov.dev/images/unknown-item-icon.jpg"
    fun getTransparentIcon(): String = "https://assets.tarkov.dev/$id-base-image.png"

    fun getTime(): Long? {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        sdf.timeZone = TimeZone.getTimeZone("GMT")
        updated?.let {
            return sdf.parse(it)?.time
        }
        return currentTimeMillis()
    }

    fun getCheapestBuyRequirements(): BuySellPrice {
        return buyFor?.minByOrNull {
            if (it.isRequirementMet()) it.getPriceAsRoubles() else Int.MAX_VALUE
            //it.price ?: Int.MAX_VALUE
        } ?: BuySellPrice(
            "fleaMarket",
            price = 0,
            requirements = emptyList(),
            "RUB"
        )
    }

    fun getHighestSellRequirements(): BuySellPrice {
        return sellFor?.maxByOrNull {
            if (!it.isRequirementMet()) Int.MIN_VALUE else it.getPriceAsRoubles()
            //it.price ?: Int.MAX_VALUE
        } ?: BuySellPrice(
            "fleaMarket",
            price = basePrice,
            requirements = emptyList(),
            "RUB"
        )
    }

    fun isAbleToPurchase(): Boolean {
        return buyFor?.any {
            it.isRequirementMet()
        } ?: false
    }

    fun getCheapestBuy(): BuySellPrice? {
        return buyFor?.minByOrNull { it.price ?: Int.MAX_VALUE }!!
    }

    fun getHighestSell(): BuySellPrice? {
        return sellFor?.maxByOrNull { it.price ?: Int.MIN_VALUE }
    }

    fun getHighestSellTrader(): BuySellPrice? {
        return sellFor?.filterNot { it.isFleaMarket() }?.maxByOrNull { it.price ?: Int.MIN_VALUE }
    }

    fun getCheapestTrader(): BuySellPrice? {
        return buyFor?.filterNot { it.isFleaMarket() || !it.isRequirementMet() }?.minByOrNull { it.price ?: Int.MAX_VALUE }
    }

    fun getPrice(): Int {
        if (id == "59faff1d86f7746c51718c9c") {
            return getHighestSellTrader()?.getPriceAsRoubles() ?: 0
        }
        return if (avg24hPrice ?: 0 > 0) {
            avg24hPrice ?: lastLowPrice ?: 0
        } else {
            lastLowPrice ?: 0
        }
    }

    fun getLastPrice(): Int {
        if (id == "59faff1d86f7746c51718c9c") {
            return getHighestSellTrader()?.getPriceAsRoubles() ?: 0
        }
        return if (lastLowPrice ?: 0 > 0) {
            lastLowPrice ?: avg24hPrice ?: basePrice
        } else {
            avg24hPrice ?: basePrice
        }
    }

    data class Contains(
        val quantity: Int?,
        val count: Int?,
        val item: ContainsItem?
    ) : Serializable {

    }

    data class BuySellPrice(
        val source: String?,
        var price: Int?,
        val requirements: List<Requirement>,
        val currency: String?
    ) : Serializable {
        data class Requirement(
            val type: String,
            val value: Int?
        ) : Serializable

        fun isQuestLocked(): Boolean {
            return requirements.any { it.type == "questCompleted" }
        }

        fun getPriceAsCurrency(): String? {
            return price?.asCurrency(currency ?: "R")
        }

        fun getPriceAsRoubles(): Int {
            return if (currency == "USD") {
                price?.fromDtoR()?.roundToInt() ?: 0
            } else if (currency == "EUR") {
                euroToRouble(price?.toLong() ?: 0).toInt()
            } else {
                price
            } ?: 0
        }

        fun isRequirementMet(): Boolean {
            if (requirements.isEmpty()) return true
            if (price == 0) return false
            if (source == "fleaMarket") {
                val playerLevel = UserSettingsModel.playerLevel.value
                return playerLevel >= requirements.first().value ?: 1
            }
            return when (source) {
                "prapor" -> {
                    val traderLevel = UserSettingsModel.praporLevel.value.toString().toInt()
                    val requirement = requirements.find { it.type == "loyaltyLevel" }
                    return traderLevel >= requirement?.value ?: 1
                }
                "therapist" -> {
                    val traderLevel = UserSettingsModel.therapistLevel.value.toString().toInt()
                    val requirement = requirements.find { it.type == "loyaltyLevel" }
                    return traderLevel >= requirement?.value ?: 1
                }
                "fence" -> {
                    val traderLevel = UserSettingsModel.fenceLevel.value.toString().toInt()
                    val requirement = requirements.find { it.type == "loyaltyLevel" }
                    return traderLevel >= requirement?.value ?: 1
                }
                "skier" -> {
                    val traderLevel = UserSettingsModel.skierLevel.value.toString().toInt()
                    val requirement = requirements.find { it.type == "loyaltyLevel" }
                    return traderLevel >= requirement?.value ?: 1
                }
                "peacekeeper" -> {
                    val traderLevel = UserSettingsModel.peacekeeperLevel.value.toString().toInt()
                    val requirement = requirements.find { it.type == "loyaltyLevel" }
                    return traderLevel >= requirement?.value ?: 1
                }
                "mechanic" -> {
                    val traderLevel = UserSettingsModel.mechanicLevel.value.toString().toInt()
                    val requirement = requirements.find { it.type == "loyaltyLevel" }
                    return traderLevel >= requirement?.value ?: 1
                }
                "ragman" -> {
                    val traderLevel = UserSettingsModel.ragmanLevel.value.toString().toInt()
                    val requirement = requirements.find { it.type == "loyaltyLevel" }
                    return traderLevel >= requirement?.value ?: 1
                }
                "jaeger" -> {
                    val traderLevel = UserSettingsModel.jaegerLevel.value.toString().toInt()
                    val requirement = requirements.find { it.type == "loyaltyLevel" }
                    return traderLevel >= requirement?.value ?: 1
                }
                else -> false
            }
        }

        fun getTitle(): String {
            return if (source == "fleaMarket") {
                "Flea Market"
            } else {
                if (requirements.isNotEmpty() && requirements.first().type == "loyaltyLevel") {
                    "${source?.sourceTitle()} ${requirements.first().value?.getTraderLevel()}"
                } else {
                    source?.sourceTitle() ?: ""
                }
            }
        }

        fun isFleaMarket(): Boolean = source == "fleaMarket"
        fun isZero(): Boolean = price == 0
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

    fun calculateTax(salePrice: Long = lastLowPrice?.toLong() ?: (0).toLong(), intel: Boolean? = false): Int {
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
        return if (intel == false) tax else (tax * 0.70).roundToInt()
    }

    data class ContainsItem(
        val id: String,
        val name: String?,
        val shortName: String?,
        val iconLink: String? = "https://tarkov.dev/images/unknown-item-icon.jpg",
        val imageLink: String?,
        val gridImageLink: String? = "https://tarkov.dev/images/unknown-item-icon.jpg",
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
        val buyFor: List<BuySellPrice>?,
        val wikiLink: String?,
        val noFlea: Boolean?,
    ) : Serializable {
        fun toPricing(): Pricing {
            val item = this
            return Pricing(
                item.id,
                item.name,
                item.shortName,
                item.iconLink,
                item.imageLink,
                item.gridImageLink,
                item.avg24hPrice,
                item.basePrice,
                item.lastLowPrice,
                item.changeLast48h,
                item.low24hPrice,
                item.high24hPrice,
                item.updated,
                types = item.types,
                item.width,
                item.height,
                sellFor = item.sellFor,
                buyFor = item.buyFor,
                item.wikiLink,
                item.noFlea,
                containsItem = null
            )
        }
    }
}
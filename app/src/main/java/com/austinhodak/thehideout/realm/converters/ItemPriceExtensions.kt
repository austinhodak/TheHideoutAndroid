package com.austinhodak.thehideout.realm.converters

import com.austinhodak.thehideout.realm.models.Item
import kotlin.math.ceil
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

//isFlea
val Item.ItemPrice.isFlea: Boolean
    get() = this.vendor?.fleaMarket != null

//isTrader
val Item.ItemPrice.isTrader: Boolean
    get() = this.vendor?.traderOffer != null

fun Item.ItemPrice.icon(showLevel: Boolean? = true): String {
    return if (isFlea) {
        "https://tarkov.dev/images/flea-market-icon.jpg"
    } else {
        vendor?.traderOffer?.trader?.icon(showLevel = showLevel) ?: ""
    }
}

fun Item.instaProfit(): Int {
    val traderPrice = this.highestTraderSell()?.priceRUB ?: 0
    val fleaPrice = this.flea()?.priceRUB ?: 0
    return (traderPrice - fleaPrice)
}

fun calculateTax(basePrice: Int, sellPrice: Int, count: Int = 1, Ti: Double = 0.05, Tr: Double = 0.10): Double {
    val V0 = basePrice.toDouble()
    val VR = sellPrice.toDouble()
    var P0 = log10(V0 / VR)
    var PR = log10(VR / V0)
    val Q = 1
    var IC = 1.0

    val intelligenceCenter = 0
    val hideoutManagement = 0

    if (VR < V0) {
        P0 = P0.pow(1.08)
    }

    if (VR >= V0) {
        PR = PR.pow(1.08)
    }

    if (intelligenceCenter >= 3) {
        IC = 1 - (((.01 * hideoutManagement) + 1) * 0.3)
    }

    return ceil(
        V0 * Ti * 4.0.pow(P0) * Q + VR * Tr * 4.0.pow(PR) * Q
    ) * IC
}

fun bestPrice(item: Item?, Ti: Double = 0.05, Tr: Double = 0.10, startPrice: Int? = null): BestPrice {
    if (item?.basePrice == null) {
        return BestPrice()
    }

    val testPrice: Int = startPrice ?: item.lastLowPrice ?: (item.basePrice * 100)
    val currentFee: Double = calculateTax(item.basePrice, testPrice, 1, Ti, Tr)
    var bestProfit = testPrice - currentFee
    var bestPrice = testPrice
    var bestPriceFee = currentFee

    for (i in testPrice - 1000 downTo 0 step 1000) {
        val newFee = calculateTax(item.basePrice, i, 1, Ti, Tr)
        val newProfit = i - newFee
        if (newProfit <= bestProfit) {
            break
        }
        bestPrice = i
        bestProfit = newProfit
        bestPriceFee = newFee
    }

    return BestPrice(
        bestPrice = bestPrice,
        bestPriceFee = bestPriceFee.roundToInt()
    )
}

data class BestPrice(
    val bestPrice: Int = 0,
    val bestPriceFee: Int = 0,
    val profit: Int = bestPrice - bestPriceFee
)
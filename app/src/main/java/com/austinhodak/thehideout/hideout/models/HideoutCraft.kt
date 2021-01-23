package com.austinhodak.thehideout.hideout.models

import com.austinhodak.thehideout.flea_market.models.FleaItem
import com.austinhodak.thehideout.getPrice
import kotlin.math.roundToInt

data class HideoutCraft (
    val facility: Int,
    val id: Int,
    val input: List<Input>,
    val output: List<Output>,
    val time: Double
) {
    fun getOutputItem(): FleaItem {
        return output[0].fleaItem
    }

    fun getTotalCostToCraft(): Int {
        var total = 0.0

        for (i in input) {
            val item = i.fleaItem
            val cost = item.price!! * i.qty
            total += cost
        }

        return total.roundToInt()
    }

    fun getTimeToCraft(): String {
        val hours = time.toInt()
        val minutes = ((time * 60) % 60).roundToInt()
        return "Crafting Time: ${hours}h ${minutes}m"
    }

    fun getOutputName(): String {
        return "Sell: x${output[0].qty} ${output[0].fleaItem.shortName}"
    }

    fun getOutputPrice(): String? {
        return (getOutputItem().price?.times(output[0].qty)?.getPrice("₽"))
    }

    fun getProfit(): Int {
        return (getOutputItem().price!! * output[0].qty - getTotalCostToCraft())
    }

    fun getTotalProfit(): Int {
        var profit = 0
        getOutputItem().calculateTax {
            val tax = it * output[0].qty
            profit = (getOutputItem().price!! * output[0].qty - getTotalCostToCraft() - tax)
        }
        return profit
    }

    fun getProfitPerHour(): Int {
        var profit = 0
        getOutputItem().calculateTax {
            val tax = it * output[0].qty
            profit = ((getOutputItem().price!! * output[0].qty - getTotalCostToCraft() - tax) / time).roundToInt()
        }
        return profit
    }
}

data class Input (
    val id: String,
    val qty: Double,
    var fleaItem: FleaItem
)

data class Output (
    val id: String,
    val qty: Int,
    var fleaItem: FleaItem
)
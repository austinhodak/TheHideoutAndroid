package com.austinhodak.thehideout.flea_market.models

data class Barter(
    val requiredItems: List<RequiredItem>,
    val rewardItems: List<RewardItem>,
    val source: String
) {
    fun rewardItem(): RewardItem = rewardItems.first()

    data class RequiredItem(
        val count: Int,
        val item: Item
    )

    data class RewardItem(
        val count: Int,
        val item: Item
    )

    data class Item(
        val id: String,
        val name: String,
        val iconLink: String,
        val traderPrices: List<TraderPrice>
    ) {
        data class TraderPrice(
            val price: Int,
            val trader: Trader
        ) {
            data class Trader(
                val id: String,
                val name: String
            )
        }
    }

    fun isNeededForAny(id: String): Boolean {
        if (requiredItems.any { it.item.id == id }) return true
        if (rewardItems.any { it.item.id == id }) return true
        return false
    }

    fun getTraderImageURL(): String = "https://cdn.tarkov-market.com/images/traders/${source.split(" ")[0]}_${source.split(" ")[1].removePrefix("LL")}"

}
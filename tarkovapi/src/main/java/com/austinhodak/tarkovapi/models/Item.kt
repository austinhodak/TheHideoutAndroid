package com.austinhodak.tarkovapi.models

data class Item(
    val avg24hPrice: Int,
    val basePrice: Int,
    val changeLast48h: Double,
    val gridImageLink: String,
    val high24hPrice: Int,
    val iconLink: String,
    val id: String,
    val imageLink: String,
    val lastLowPrice: Int,
    val low24hPrice: Int,
    val name: String,
    val shortName: String,
    val traderPrices: List<TraderPrice>,
    val updated: String,
    val types: List<String>
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
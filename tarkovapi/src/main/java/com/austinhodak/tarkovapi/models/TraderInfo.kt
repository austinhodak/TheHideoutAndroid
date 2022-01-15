package com.austinhodak.tarkovapi.models

data class TraderInfo(
    val currencies: List<String>,
    val description: String,
    val id: Int,
    val locale: Locale,
    val loyalty: List<Loyalty>,
    val name: String,
    val salesCurrency: String,
    val wiki: String
) {
    data class Locale(
        val en: String
    )

    data class Loyalty(
        val level: Int,
        val requiredLevel: Int,
        val requiredReputation: Double,
        val requiredSales: Int
    )
}
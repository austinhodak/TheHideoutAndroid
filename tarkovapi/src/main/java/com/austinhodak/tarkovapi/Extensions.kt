package com.austinhodak.tarkovapi

import java.text.NumberFormat
import java.util.*

fun Int.asCurrency(currency: String = "R"): String {
    val numFormat = NumberFormat.getCurrencyInstance().apply {
        maximumFractionDigits = 0
    }

    numFormat.currency = when (currency) {
        "R" -> Currency.getInstance("RUB")
        "D" -> Currency.getInstance("USD")
        "E" -> Currency.getInstance("EUR")
        else -> Currency.getInstance("RUB")
    }

    var formatted = numFormat.format(this)

    if (currency == "R") {
        formatted = formatted.replace("RUB", "").plus("₽")
    }

    return formatted
}
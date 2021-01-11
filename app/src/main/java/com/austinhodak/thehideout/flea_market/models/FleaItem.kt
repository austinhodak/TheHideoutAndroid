package com.austinhodak.thehideout.flea_market.models

import android.text.format.DateUtils
import com.austinhodak.thehideout.getPrice
import java.text.SimpleDateFormat
import java.util.*

data class FleaItem(
    val avg24hPrice: Int,
    val avg7daysPrice: Int,
    val basePrice: Int,
    val bsgId: String,
    val diff24h: Double,
    val diff7days: Double,
    val icon: String,
    val img: String,
    val imgBig: String,
    val isFunctional: Boolean,
    val link: String,
    val name: String,
    val price: Int,
    val reference: String,
    val shortName: String,
    val slots: Int,
    val traderName: String,
    val traderPrice: Int,
    val traderPriceCur: String,
    val uid: String,
    val updated: String,
    val wikiLink: String
) {
    fun getPrice(): String {
        return price.getPrice(traderPriceCur)
    }

    fun getPricePerSlot(): String {
        return "${(price/slots).getPrice(traderPriceCur)}/slot"
    }

    fun getUpdatedTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        sdf.timeZone = TimeZone.getTimeZone("GMT")
        return "Updated ${DateUtils.getRelativeTimeSpanString(sdf.parse(updated).time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)}"
    }
}
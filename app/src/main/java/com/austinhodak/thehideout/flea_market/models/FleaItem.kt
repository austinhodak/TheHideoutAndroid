package com.austinhodak.thehideout.flea_market.models

import android.text.format.DateUtils
import com.austinhodak.thehideout.getPrice
import com.austinhodak.thehideout.hideout.models.Input
import com.austinhodak.thehideout.viewmodels.FleaViewModel
import com.google.firebase.database.IgnoreExtraProperties
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

@IgnoreExtraProperties
data class FleaItem(
    val avg24hPrice: Int? = null,
    val avg7daysPrice: Int? = null,
    val basePrice: Int? = null,
    val bsgId: String? = null,
    val diff24h: Double? = null,
    val diff7days: Double? = null,
    val icon: String? = null,
    val img: String? = null,
    val imgBig: String? = null,
    val isFunctional: Boolean? = null,
    val link: String? = null,
    val name: String? = null,
    val price: Int? = null,
    val reference: String? = null,
    val shortName: String? = null,
    val slots: Int? = 1,
    val traderName: String? = null,
    val traderPrice: Int? = null,
    val traderPriceCur: String? = null,
    val uid: String? = null,
    val updated: String? = null,
    val wikiLink: String? = null,
) {
    fun getCurrentPrice(): String {
        return price?.getPrice("₽")!!
    }

    fun getCurrentTraderPrice(): String {
        return traderPrice?.getPrice(traderPriceCur!!)!!
    }

    fun getPricePerSlot(): String {
        return "${(price!!/slots!!).getPrice("₽")}/slot"
    }

    fun getItemIcon(): String {
        return if (icon.isNullOrEmpty() && img.isNullOrEmpty()) {
            ""
        } else if (icon.isNullOrEmpty()) {
            img ?: ""
        } else if (img.isNullOrEmpty()) {
            icon
        } else icon
    }

    fun getUpdatedTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        sdf.timeZone = TimeZone.getTimeZone("GMT")
        return "Updated ${DateUtils.getRelativeTimeSpanString(sdf.parse(updated).time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)}"
    }

    fun getTotalCostToCraft(input: List<Input>, vm: FleaViewModel): Int {
        var total = 0.0

        for (i in input) {
            val item = vm.getItemById(i.id)
            val cost = item.price!! * i.qty
            total += cost
        }

        return total.roundToInt()
    }

    fun calculateTax(salePrice: Int = price ?: 0, callback: (result: Int) -> Unit) {
        val mVO = basePrice!!.toDouble()
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
        callback.invoke(tax)
    }
}
package com.austinhodak.thehideout.realm.converters

import android.text.format.DateUtils
import androidx.tracing.trace
import com.austinhodak.thehideout.fragment.ItemFragment
import com.austinhodak.thehideout.realm.models.Item
import com.austinhodak.thehideout.type.ItemType
import timber.log.Timber
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Currency
import java.util.TimeZone
import kotlin.math.roundToInt

class ItemWrapper (
    val item: ItemFragment
) {

    val name = item.name
    val shortName = item.shortName

    val id: String = item.id

    val noFlea: Boolean = item.types.contains(ItemType.noFlea)
    val fleaBanned: Boolean = noFlea

    val cleanIcon: String = item.iconLink ?: item.gridImageLink ?: ""
    val icon: String = item.gridImageLink ?: item.iconLink ?: ""

    val getPrice: Int = item.lastLowPrice ?: 0

/*    val flea: ItemPrice? = item.buyFor?.firstOrNull {
        !it.isTrader
    }*/

    fun updatedTime(resolution: Long = DateUtils.MINUTE_IN_MILLIS, ): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        sdf.timeZone = TimeZone.getTimeZone("GMT")

        val date = sdf.parse(item.updated ?: "2021-07-01T08:36:35.194Z") ?: Calendar.getInstance().time
        val currentTime = System.currentTimeMillis()
        val timeString = DateUtils.getRelativeTimeSpanString(date.time, currentTime, resolution, DateUtils.FORMAT_ABBREV_RELATIVE).toString()

        return timeString.removeSuffix(". ago")
    }

    val totalSize: Int = (item.width * item.height)
    val pricePerSlot: Int = (getPrice / totalSize)

/*    val sellTraders: List<ItemPrice>? = item.sellFor?.filter { it.isTrader }
    val buyTraders: List<ItemPrice>? = item.buyFor?.filter { it.isTrader }*/

/*    val highestTraderSell: ItemPrice? = sellTraders?.maxByOrNull { it.priceRUB ?: 0 }
    val lowestTraderBuy: ItemPrice? = buyTraders?.minByOrNull { it.priceRUB ?: 0 }*/

}

fun ItemFragment.toWrapper() = ItemWrapper(this)

fun <T> T.formatPrice(forceRuble: Boolean = false): String {
    val formatted = trace<String>("formatPrice") {

        Timber.d("formatPrice: $this")
        val numFormat = NumberFormat.getCurrencyInstance().apply {
            maximumFractionDigits = 0
            currency = Currency.getInstance("RUB")
        }

        var formatted = when (this) {
            is Int -> numFormat.format(this)
            is Double -> numFormat.format(this.roundToInt())
            is Item.ItemPrice -> {
                numFormat.currency = when {
                    forceRuble -> Currency.getInstance("RUB")
                    else -> Currency.getInstance(currency)
                }

                numFormat.format(
                    when {
                        forceRuble -> priceRUB
                        else -> price
                    }
                )
            }
            //is ItemWrapper -> getPrice.formatPrice()
            is Item -> lastLowPrice.formatPrice()
            else -> {
                "N/A"
            }
        }

        if (formatted.contains("RUB")) {
            formatted = formatted.replace("RUB", "").plus("₽")
        }
        formatted
    }

    return formatted
}
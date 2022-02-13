package com.austinhodak.tarkovapi.models

import android.text.format.DateUtils
import com.austinhodak.tarkovapi.TraderResetTimersQuery
import java.text.SimpleDateFormat
import java.util.*

data class TraderReset(
    val traderResetTimes: List<TraderResetTime>
) {
    data class TraderResetTime(
        val name: String?,
        val resetTimestamp: String?,
        var restockTime: String?
    ) {
        fun getResetTimeSpan(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            sdf.timeZone = TimeZone.getTimeZone("GMT")
            val resetTimeMillis = sdf.parse(resetTimestamp ?: "")?.time ?: 0
            val currentTimeMillis = System.currentTimeMillis()

            if (resetTimeMillis <= currentTimeMillis) {
                return "Restocked ${
                    DateUtils.getRelativeTimeSpanString(
                        resetTimeMillis,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                    )
                }"
            }

            val remainingTime = resetTimeMillis - currentTimeMillis
            val newDateFormat = SimpleDateFormat("HH:mm:ss")
            newDateFormat.timeZone = TimeZone.getTimeZone("GMT")
            return "Restocks in ${newDateFormat.format(remainingTime)}"
        }
    }

    fun getTrader(name: String): TraderResetTime? {
        return traderResetTimes.find { it.name.equals(name, true) }
    }

    fun updateTimes(): TraderReset {
        traderResetTimes.forEach {
            it.restockTime = it.getResetTimeSpan()
        }
        return this
    }
}

fun TraderResetTimersQuery.Data.toObj(): TraderReset {
    return TraderReset(
        traderResetTimes = traderResetTimes?.map {
            TraderReset.TraderResetTime(
                it?.name,
                it?.resetTimestamp,
                null
            )
        } ?: emptyList()
    )
}
package com.austinhodak.tarkovapi.models

import android.text.format.DateUtils
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.austinhodak.tarkovapi.R
import com.austinhodak.tarkovapi.ServerStatusQuery
import java.text.SimpleDateFormat
import java.util.*

val StatusBlue = Color(0xFF90c1eb)
val StatusRed = Color(0xFFd42929)
val StatusOrange = Color(0xFFca8a00)
val StatusGreen = Color(0xFF70b035)

data class ServerStatus(
    val currentStatuses: List<CurrentStatuses>?,
    val generalStatus: GeneralStatus?,
    val messages: List<Message>?
) {
    data class CurrentStatuses(
        val message: Any?,
        val name: String?,
        val status: Int?
    ) {
        fun getColor(): Color {
            return when (status) {
                0 -> StatusGreen
                1 -> StatusBlue
                2 -> StatusOrange
                3 -> StatusRed
                else -> StatusRed
            }
        }

        fun getStatusDescription(): String {
            return when (status) {
                0 -> "Operational"
                1 -> "Updating"
                2 -> "Unstable"
                3, 4 -> "Issues"
                else -> ""
            }
        }
    }

    data class GeneralStatus(
        val message: String?,
        val name: String?,
        val status: Int?
    )

    fun getBadgeText(): String {
        return when (generalStatus?.status) {
            0 -> ""
            1 -> " Updating "
            2 -> " Problems "
            3, 4 -> " Issues "
            else -> ""
        }
    }

    data class Message(
        val content: String?,
        val solveTime: String?,
        val time: String?,
        val type: Int?
    ) {
        fun getStatusDescription(): String {
            return when (type) {
                0 -> "Info"
                1 -> "Update installation"
                2 -> "Server Issues"
                else -> "Server Issues"
            }
        }

        fun getStatus(): String {
            return if (solveTime == null) {
                "Ongoing Issue"
            } else {
                "Issue Resolved ${getResolvedTime()}"
            }
        }

        fun getColor(): Color {
            return when (type) {
                0 -> StatusGreen
                1 -> StatusBlue
                2 -> StatusOrange
                3 -> StatusRed
                else -> StatusRed
            }
        }

        @DrawableRes
        fun getIcon(): Int {
            return when (type) {
                1 -> R.drawable.icons8_installing_updates_96
                2, 3, 4 -> R.drawable.icons8_error_96
                else -> R.drawable.icons8_info_96
            }
        }

        fun getMessageTime(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
            sdf.timeZone = TimeZone.getTimeZone("GMT")
            return "Updated ${
                DateUtils.getRelativeTimeSpanString(
                    sdf.parse(time ?: "2021-07-01T08:36:35.194Z")?.time ?: 0,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
                )
            }"
        }

        fun getResolvedTime(): String? {
            if (solveTime == null) return null
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
            sdf.timeZone = TimeZone.getTimeZone("GMT")
            return "${
                DateUtils.getRelativeTimeSpanString(
                    sdf.parse(solveTime)?.time ?: 0,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
                )
            }"
        }

        fun isResolved(): Boolean = solveTime != null
    }

    fun currentStatusColor(): Color {
        return when (generalStatus?.status) {
            0 -> StatusGreen
            1 -> StatusBlue
            2 -> StatusOrange
            3 -> StatusRed
            else -> StatusRed
        }
    }

    fun isDegraded(): Boolean {
        return generalStatus?.status != 0
    }


}

fun ServerStatusQuery.Status.toObj(): ServerStatus {
    return ServerStatus(
        generalStatus = ServerStatus.GeneralStatus(
            this.generalStatus?.message,
            this.generalStatus?.name,
            this.generalStatus?.status
        ),
        currentStatuses = this.currentStatuses?.map {
            ServerStatus.CurrentStatuses(
                it?.message,
                it?.name,
                it?.status
            )
        },
        messages = this.messages?.map {
            ServerStatus.Message(
                it?.content,
                it?.solveTime,
                it?.time,
                it?.type
            )
        }
    )
}
package com.austinhodak.thehideout.features.news.models


import android.text.format.DateUtils
import androidx.annotation.DrawableRes
import com.austinhodak.thehideout.R
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

data class NewsItem(
    @SerializedName("data")
    val `data`: List<Data>
) {
    data class Data(
        @SerializedName("account")
        val account: Account,
        @SerializedName("content")
        val content: String,
        @SerializedName("id")
        val id: String,
        @SerializedName("section")
        val section: Any,
        @SerializedName("timestamp")
        val timestamp: Long,
        @SerializedName("topic")
        val topic: String,
        @SerializedName("topicUrl")
        val topicUrl: Any,
        @SerializedName("url")
        val url: String,
        @SerializedName("urlHash")
        val urlHash: String
    ) {
        data class Account(
            @SerializedName("developer")
            val developer: Developer,
            @SerializedName("identifier")
            val identifier: String,
            @SerializedName("service")
            val service: String
        ) {
            data class Developer(
                @SerializedName("group")
                val group: String,
                @SerializedName("name")
                val name: Any,
                @SerializedName("nick")
                val nick: String,
                @SerializedName("role")
                val role: Any
            )

            @DrawableRes
            fun getIcon(): Int {
                return when (service.lowercase()) {
                    "twitter" -> R.drawable.icons8_twitter_color_dual
                    "reddit" -> R.drawable.icons8_reddit_color_dual
                    else -> R.drawable.icons8_reddit_color_dual
                }
            }

            fun getTitle(): String {
                return when (service.lowercase()) {
                    "twitter" -> {
                        "${developer.nick} Tweeted"
                    }
                    "reddit" -> {
                        "${developer.nick} (${developer.role} - ${developer.group})"
                    }
                    else -> {
                        ""
                    }
                }
            }
        }

        fun getMessageTime(): String {
            return "${
                DateUtils.getRelativeTimeSpanString(
                    (timestamp.toString() + "000").toLong(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
                )
            }"
        }
    }
}
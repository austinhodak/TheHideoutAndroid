package com.austinhodak.thehideout.news.models


import com.google.gson.annotations.SerializedName

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
        val timestamp: Int,
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
        }
    }
}
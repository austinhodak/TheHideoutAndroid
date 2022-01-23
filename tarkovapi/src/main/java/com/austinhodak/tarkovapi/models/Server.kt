package com.austinhodak.tarkovapi.models

import androidx.compose.ui.graphics.Color
import com.austinhodak.tarkovapi.*

data class Server(
    val ip: String,
    val name: String,
    val region: String,
    var ping: Long,
    var inProgress: Boolean = false
) {
    fun pingColor(): Color {
        return when  {
            ping >= 500 -> Ping500
            ping >= 400 -> Ping400
            ping >= 300 -> Ping300
            ping >= 200 -> Ping200
            ping >= 100 -> Ping100
            ping >= 80 -> Ping80
            ping >= 1 -> Ping0
            ping >= 0 -> Color.Unspecified
            else -> {
                Color.Unspecified
            }
        }
    }
}
package com.austinhodak.thehideout.hideout.models

import kotlin.math.roundToInt

data class HideoutCraft (
    val facility: Int,
    val id: Int,
    val input: List<Input>,
    val output: List<Output>,
    val time: Double
) {
    fun getTimeToCraft(): String {
        val hours = time.toInt()
        val minutes = ((time * 60) % 60).roundToInt()
        return "Crafting Time: ${hours}h ${minutes}m"
    }
}

data class Input (
    val id: String,
    val qty: Double
)

data class Output (
    val id: String,
    val qty: Int
)
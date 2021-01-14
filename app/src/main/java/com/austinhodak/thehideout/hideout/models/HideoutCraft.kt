package com.austinhodak.thehideout.hideout.models

data class HideoutCraft (
    val facility: Int,
    val id: Int,
    val input: List<Input>,
    val output: List<Output>,
    val time: Double
)

data class Input (
    val id: String,
    val qty: Int
)

data class Output (
    val id: String,
    val qty: Int
)
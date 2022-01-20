package com.austinhodak.tarkovapi.models

data class AmmoBallistic(
    val ballistics: List<Ballistics>,
    val id: String
) {
    data class Ballistics(
        val damage: String,
        val drop: String,
        val penetration: String,
        val range: String,
        val tof: String,
        val velocity: String
    )
}
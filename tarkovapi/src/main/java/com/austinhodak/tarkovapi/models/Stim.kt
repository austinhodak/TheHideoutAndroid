package com.austinhodak.tarkovapi.models

data class Stim(
    val stats: List<Stat>,
    val stim: String
) {
    data class Stat(
        val AbsoluteValue: Boolean,
        val AppliesTo: List<String>,
        val BuffType: String,
        val Chance: Double,
        val Delay: Int,
        val Duration: Int,
        val SkillName: String,
        val Value: Double
    )
}
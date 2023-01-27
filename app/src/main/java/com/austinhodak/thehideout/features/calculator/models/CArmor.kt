package com.austinhodak.thehideout.features.calculator.models

data class CArmor(
    val customDurability: Double = 0.0,
    val `class`: Int = 0,
    val bluntThroughput: Double = 0.0,
    var durability: Double = 0.0,
    val maxDurability: Double = 0.0,
    val resistance: Double = 0.0,
    val destructibility: Double = 0.0,
    val zones: List<String> = ArrayList()
)
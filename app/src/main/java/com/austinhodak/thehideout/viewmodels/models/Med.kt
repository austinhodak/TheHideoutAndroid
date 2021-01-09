package com.austinhodak.thehideout.viewmodels.models

data class Med(
    var icon: String,
    var name: String,
    var type: String,
    var effect: String,
    var use_time: Int,
    var uses: Int,
    var hp_pool: Int,
    var max_hp_heal: Int
)
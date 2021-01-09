package com.austinhodak.thehideout.viewmodels.models

data class Stim(
    var icon: String,
    var name: String,
    var type: String,
    var buffs: String,
    var debuffs: String,
    var use_time: Int
)
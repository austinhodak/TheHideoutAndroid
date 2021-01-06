package com.austinhodak.thehideout.viewmodels.models

data class CaliberModel (
    var description: String,
    var _id: String,
    var name: String,
    var image: String,
    var ammo: List<AmmoModel>,
    var long_name: String
)
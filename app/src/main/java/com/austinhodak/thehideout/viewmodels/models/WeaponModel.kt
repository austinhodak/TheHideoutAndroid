package com.austinhodak.thehideout.viewmodels.models

data class WeaponModel(
    var grid: WeaponGrid,
    var firemodes: WeaponFiremodes,
    var recoil: WeaponRecoil,
    var description: String,
    var weight: Double,
    var _id: String,
    var name: String,
    var `class`: String,
    var image: String,
    var rpm: Long,
    var range: Long,
    var ergonomics: Long,
    var accuracy: Double,
    var velocity: Double,
    var fields: List<WeaponField>,
    var calibre: String,
    var __v: Int,
    var builds: List<WeaponBuilds>
)

data class WeaponBuilds(
    var attachments: List<String>,
    var prices: List<WeaponPrices>,
    var tradeups: List<WeaponTradeups>,
    var name: String,
    var image: String,
    var _id: String
) {

    data class WeaponPrices(
        var value: Double,
        var _id: String,
        var trader: String,
        var currency: String,
        var level: Int
    )

    data class WeaponTradeups(
        var items: List<String>,
        var _id: String,
        var trader: String,
        var level: Int
    )
}

data class WeaponField(
    var vital: Boolean,
    var attachments: List<String>,
    var _id: String,
    var name: String
)

data class WeaponRecoil(
    var horizontal: Long,
    var vertical: Long
)

data class WeaponFiremodes(
    var single: Boolean,
    var burst: Boolean,
    var auto: Boolean
)

data class WeaponGrid(
    var x: Int,
    var y: Int
)
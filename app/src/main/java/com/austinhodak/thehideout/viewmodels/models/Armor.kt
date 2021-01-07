package com.austinhodak.thehideout.viewmodels.models

data class Armor(
    var description: String,
    var weight: Double,
    var level: Int,
    var hitpoints: Int,
    var movement: Int,
    var turn: Int,
    var ergonomics: Int,
    var zones: List<String>,
    var _id: String,
    var name: String,
    var image: String,
    var material: String,
    var `class`: String,
    var ricochet: String,
    var grid: Grid,
    var prices: List<Price>,
    var tradeups: List<Tradeup>,
    var fields: List<Field>,
    var internal: Int
) {
    data class Field(
        var vital: Boolean,
        var _id: String,
        var name: String
    )

    fun getSubtitle(): String {
        var string = ""
        for (i in prices) {
            string += "\n$i"
        }

        for (i in tradeups) {
            string += "\n$i"
        }

        return string.trim()
    }
}
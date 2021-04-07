package com.austinhodak.thehideout.clothing.models

data class Rig(
    var grid: Grid,
    var weight: Double,
    var armored: Boolean,
    var _id: String,
    var name: String,
    var image: String,
    var internal: Int,
    var prices: List<Price>,
    var tradeups: List<Tradeup>
) {
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

    fun getArmored(): String {
        return if (armored) "YES" else "NO"
    }

    fun getImageURL(): String {
        return if (image.contains("https")) return image else "https://eftdb.one/static/item/thumb/$image"
    }
}
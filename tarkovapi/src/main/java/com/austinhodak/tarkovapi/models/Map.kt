package com.austinhodak.tarkovapi.models

data class Map(
    val description: String? = null,
    val enemies: List<String?>? = null,
    val id: Int? = null,
    val locale: Locale? = null,
    val raidDuration: RaidDuration? = null,
    val svg: Svg? = null,
    val wiki: String? = null
) {
    override fun toString(): String {
        return locale?.en ?: "Any Map"
    }

    data class Locale(
        val en: String? = null
    )

    data class RaidDuration(
        val day: Int? = null,
        val night: Int? = null
    )

    data class Svg(
        val defaultFloor: String? = null,
        val `file`: String? = null,
        val floors: List<String?>? = null
    )
}
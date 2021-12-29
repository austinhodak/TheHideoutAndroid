package com.austinhodak.tarkovapi.models

data class WeaponPreset(
    val baseId: String,
    val default: Boolean,
    val id: String,
    val name: String,
    val parts: List<Part>
) {
    data class Part(
        val id: String,
        val quantity: Int
    )
}
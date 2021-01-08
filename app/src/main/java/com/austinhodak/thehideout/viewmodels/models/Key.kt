package com.austinhodak.thehideout.viewmodels.models

data class Key(
    val icon: String,
    val name: String,
    val link: String,
    val map: String,
    val location: String,
    val door: String,
    val details: List<String>,
    val _id: String
) {
    fun getDetails(): String {
        return details.joinToString(" ")
    }
}
package com.austinhodak.tarkovapi.models

import java.io.Serializable

data class Skill(
    val effects: List<String>,
    val icon: String,
    val name: String,
    val raise: List<String>,
    val type: String,
    val live: Boolean,
    val description: String,
    val wiki: String
) : Serializable
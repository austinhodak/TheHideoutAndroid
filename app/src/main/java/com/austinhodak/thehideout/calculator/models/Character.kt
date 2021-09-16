package com.austinhodak.thehideout.calculator.models

import java.io.Serializable

data class Character(
    val name: String,
    val health: Health,
    val image: String,
    val c_type: String,
    val spawn_chance: Int? = null
) : Serializable {

    data class Health(
        val head: Int,
        val thorax: Int,
        val stomach: Int,
        val arms: Int,
        val legs: Int
    ) : Serializable {

        override fun toString(): String {
            return (head + thorax + stomach + (arms * 2) + (legs * 2)).toString()
        }

    }
}
package com.austinhodak.tarkovapi.room.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "traders")
data class Trader(
    @PrimaryKey
    val name: String,
    val level: Int? = null
)
package com.austinhodak.tarkovapi.room.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pricing_table")
data class Price (
    @PrimaryKey var id: String,
    val pricing: Pricing?
)
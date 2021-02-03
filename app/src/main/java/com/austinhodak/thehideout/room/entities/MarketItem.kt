package com.austinhodak.thehideout.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity (tableName = "fleaMarket")
data class MarketItem (
    val avg24hPrice: Int?,
    val avg7daysPrice: Int?,
    val basePrice: Int?,
    val bsgId: String?,
    val diff24h: Double?,
    val diff7days: Double?,
    val icon: String?,
    val img: String?,
    val imgBig: String?,
    val isFunctional: Boolean?,
    val link: String?,
    val name: String?,
    val price: Int?,
    val reference: String?,
    val shortName: String?,
    val slots: Int? = 1,
    val traderName: String?,
    val traderPrice: Int?,
    val traderPriceCur: String?,
    @PrimaryKey val uid: String,
    val updated: String?,
    val wikiLink: String?,
)
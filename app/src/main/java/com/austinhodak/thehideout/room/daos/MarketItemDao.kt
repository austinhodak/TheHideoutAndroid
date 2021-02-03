package com.austinhodak.thehideout.room.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.austinhodak.thehideout.room.entities.MarketItem

@Dao
interface MarketItemDao {
    @Query ("SELECT * FROM fleaMarket")
    fun getAll(): List<MarketItem>

    @Query("SELECT * FROM fleaMarket WHERE :id = uid")
    fun findByID(id: String): MarketItem

    @Insert
    fun insertAll(vararg items: MarketItem)
}
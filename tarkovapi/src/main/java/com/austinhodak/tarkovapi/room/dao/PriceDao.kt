package com.austinhodak.tarkovapi.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.austinhodak.tarkovapi.room.models.Price

@Dao
interface PriceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(price: List<Price>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(price: Price)

    @Query("select count(*) from pricing_table")
    fun count(): Int
}
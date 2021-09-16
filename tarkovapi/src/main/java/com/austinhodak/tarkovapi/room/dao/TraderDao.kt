package com.austinhodak.tarkovapi.room.dao

import androidx.room.*
import com.austinhodak.tarkovapi.room.models.Trader
import kotlinx.coroutines.flow.Flow

@Dao
interface TraderDao {

    @Transaction
    @Query("SELECT * FROM traders WHERE name = :name")
    fun getTrader(name: String): Flow<Trader>

    @Query("UPDATE traders SET level = :level WHERE name = :name")
    suspend fun updateTrader(level: Int, name: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: Trader)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(data: List<Trader>)

}
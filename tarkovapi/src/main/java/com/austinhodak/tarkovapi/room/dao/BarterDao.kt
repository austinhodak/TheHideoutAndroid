package com.austinhodak.tarkovapi.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.austinhodak.tarkovapi.room.models.Barter
import kotlinx.coroutines.flow.Flow

@Dao
interface BarterDao {

    @Query("SELECT * FROM barters")
    fun getAllCrafts(): List<Barter>

    @Query("SELECT * FROM barters WHERE requiredItems LIKE :id OR rewardItems LIKE :id")
    fun getBartersWithItemID(id: String): Flow<List<Barter>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(crafts: List<Barter>?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(barter: Barter)

}
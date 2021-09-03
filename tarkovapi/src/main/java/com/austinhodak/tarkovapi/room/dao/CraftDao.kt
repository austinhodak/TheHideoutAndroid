package com.austinhodak.tarkovapi.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.austinhodak.tarkovapi.room.models.Craft
import kotlinx.coroutines.flow.Flow

@Dao
interface CraftDao {

    @Query("SELECT * FROM crafts")
    fun getAllCrafts(): List<Craft>

    @Query("SELECT * FROM crafts WHERE requiredItems LIKE :id OR rewardItems LIKE :id")
    fun getCraftsWithItemID(id: String): Flow<List<Craft>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(craft: Craft)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(crafts: List<Craft>?)

   /* @Query("DELETE FROM crafts")
    fun nukeTable()*/
}
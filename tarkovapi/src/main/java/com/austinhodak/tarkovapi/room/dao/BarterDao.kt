package com.austinhodak.tarkovapi.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.austinhodak.tarkovapi.room.models.Barter

@Dao
interface BarterDao {

    @Query("SELECT * FROM barters")
    fun getAllCrafts(): List<Barter>

    @Query("SELECT * FROM barters WHERE requiredItems LIKE :id OR rewardItems LIKE :id")
    fun getBartersWithItemID(id: String): LiveData<List<Barter>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(crafts: List<Barter>?)

   /* @Query("DELETE FROM crafts")
    fun nukeTable()*/
}
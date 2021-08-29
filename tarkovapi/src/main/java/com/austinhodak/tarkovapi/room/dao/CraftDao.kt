package com.austinhodak.tarkovapi.room.dao

import androidx.room.Dao

@Dao
interface CraftDao {

   /* @Query("SELECT * FROM crafts")
    fun getAllCrafts(): List<Craft>

    @Query("SELECT * FROM crafts WHERE requiredItems LIKE :id OR rewardItems LIKE :id")
    fun getCraftsWithItemID(id: String): LiveData<List<Craft>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(craft: Craft)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(crafts: List<Craft>?)*/

   /* @Query("DELETE FROM crafts")
    fun nukeTable()*/
}
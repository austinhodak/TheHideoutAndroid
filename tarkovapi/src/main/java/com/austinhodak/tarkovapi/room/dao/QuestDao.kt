package com.austinhodak.tarkovapi.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.austinhodak.tarkovapi.room.models.Quest

@Dao
interface QuestDao {

    @Query("SELECT * FROM quests")
    fun getAlLQuests(): List<Quest>

    @Query("SELECT * FROM quests WHERE objective LIKE :id")
    fun getQuestsWithItemID(id: String): LiveData<List<Quest>>

    @Query("SELECT * FROM quests WHERE id IS :id")
    fun getQuestByID(id: String): LiveData<Quest>

    @Query("SELECT EXISTS (SELECT * FROM quests WHERE id IS '195' AND requirement_quests LIKE '%' + :id + '%')")
    fun isQuestRequiredForKappa(id: String): LiveData<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(craft: Quest)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(crafts: List<Quest>?)

   /* @Query("DELETE FROM quests")
    fun nukeTable()*/
}
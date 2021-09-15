package com.austinhodak.tarkovapi.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.austinhodak.tarkovapi.room.models.Quest
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestDao {

    @Query("SELECT * FROM quests")
    fun getAlLQuests(): Flow<List<Quest>>

    @Query("SELECT * FROM quests")
    suspend fun getAlLQuestsOnce():List<Quest>

    @Query("SELECT * FROM quests WHERE objective LIKE :id")
    fun getQuestsWithItemID(id: String): Flow<List<Quest>>

    @Query("SELECT * FROM quests WHERE objective LIKE :id")
    fun getQuestWithObjectiveID(id: String): Flow<Quest>

    @Query("SELECT * FROM quests WHERE id IS :id")
    fun getQuestByID(id: String): Flow<Quest>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(craft: Quest)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(crafts: List<Quest>?)

    @Query("SELECT EXISTS (SELECT * FROM quests WHERE id IS '195' AND requirement LIKE '%' + :id + '%')")
    fun isQuestRequiredForKappa(id: String): Flow<Int>

    @Query("SELECT COUNT (id) FROM quests")
    fun numOfQuests(): Flow<Int>

    /* @Query("DELETE FROM quests")
     fun nukeTable()*/
}
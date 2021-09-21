package com.austinhodak.tarkovapi.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.austinhodak.tarkovapi.room.models.Barter
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.room.models.Mod
import kotlinx.coroutines.flow.Flow

@Dao
interface ModDao {

    @Query("SELECT * FROM mods")
    fun getAllMods(): Flow<List<Mod>>

    @Query("SELECT * FROM mods WHERE id = :id")
    fun getByID(id: String): Flow<Mod>

    @Query("SELECT * FROM mods WHERE id IN (:ids)")
    fun getByID(ids: List<String>): Flow<List<Mod>>

    @Query("SELECT * FROM mods WHERE parent = :parent")
    fun getByParent(parent: String): Flow<List<Mod>>

    @Query("SELECT * FROM mods WHERE parent IN (:parents)")
    fun getByParent(parents: List<String>): Flow<List<Mod>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mod: Mod)
}
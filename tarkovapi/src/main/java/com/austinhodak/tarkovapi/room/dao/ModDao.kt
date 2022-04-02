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

    @Query("SELECT *, (SELECT pricing FROM pricing_table WHERE pricing_table.id = mods.id) AS pricing FROM mods")
    fun getAllMods(): Flow<List<Mod>>

    @Query("SELECT *, (SELECT pricing FROM pricing_table WHERE pricing_table.id = mods.id) AS pricing FROM mods WHERE Slots LIKE :id ")
    fun getModsForSlot(id: String): Flow<List<Mod>>

    @Query("SELECT *, (SELECT pricing FROM pricing_table WHERE pricing_table.id = mods.id) AS pricing FROM mods WHERE id = :id")
    fun getByID(id: String): Flow<Mod>

    @Query("SELECT *, (SELECT pricing FROM pricing_table WHERE pricing_table.id = mods.id) AS pricing FROM mods WHERE id IN (:ids)")
    fun getByID(ids: List<String>): Flow<List<Mod>>

    @Query("SELECT *, (SELECT pricing FROM pricing_table WHERE pricing_table.id = mods.id) AS pricing FROM mods WHERE parent = :parent")
    fun getByParent(parent: String): Flow<List<Mod>>

    @Query("SELECT *, (SELECT pricing FROM pricing_table WHERE pricing_table.id = mods.id) AS pricing FROM mods WHERE parent IN (:parents)")
    fun getByParent(parents: List<String>): Flow<List<Mod>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mod: Mod)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mods: List<Mod>)
}
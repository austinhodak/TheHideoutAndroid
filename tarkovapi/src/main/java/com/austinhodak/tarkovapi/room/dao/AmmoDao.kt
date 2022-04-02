package com.austinhodak.tarkovapi.room.dao

import androidx.room.*
import com.austinhodak.tarkovapi.room.models.Ammo
import kotlinx.coroutines.flow.Flow

@Dao
interface AmmoDao {

    @Transaction
    @Query("SELECT *, (SELECT pricing FROM pricing_table WHERE pricing_table.id = ammo.id) AS pricing FROM ammo")
    fun getAllAmmo(): Flow<List<Ammo>>

    @Transaction
    @Query("SELECT *, (SELECT pricing FROM pricing_table WHERE pricing_table.id = ammo.id) AS pricing FROM ammo WHERE id = :id ORDER BY id DESC")
    fun getAmmo(id: String): Flow<Ammo>

    @Query("SELECT *, (SELECT pricing FROM pricing_table WHERE pricing_table.id = ammo.id) AS pricing FROM ammo WHERE Caliber = :caliber")
    fun getAmmoByCaliber(caliber: String): Flow<List<Ammo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(data: List<Ammo>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: Ammo)

}
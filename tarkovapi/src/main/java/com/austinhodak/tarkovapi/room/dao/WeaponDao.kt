package com.austinhodak.tarkovapi.room.dao

import androidx.room.*
import com.austinhodak.tarkovapi.room.models.Weapon
import kotlinx.coroutines.flow.Flow

@Dao
interface WeaponDao {

    @Transaction
    @Query("SELECT *, (SELECT pricing FROM pricing_table WHERE pricing_table.id = weapons.id) AS pricing FROM weapons WHERE weapClass = :classID AND BackgroundColor IS NOT NULL AND Rarity IS NOT 'Not_exist' ORDER BY ShortName")
    fun getWeaponsByClass(classID: String): Flow<List<Weapon>>

    @Transaction
    @Query("SELECT *, (SELECT pricing FROM pricing_table WHERE pricing_table.id = weapons.id) AS pricing FROM weapons WHERE id = :id")
    fun getWeapon(id: String): Flow<Weapon>

    @Transaction
    @Query("SELECT *, (SELECT pricing FROM pricing_table WHERE pricing_table.id = weapons.id) AS pricing FROM weapons")
    fun getAllWeapons(): Flow<List<Weapon>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(data: List<Weapon>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: Weapon)

}
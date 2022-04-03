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
    @Query("SELECT *, (SELECT pricing FROM pricing_table WHERE pricing_table.id = weapons.id) AS pricing, (SELECT parent FROM items WHERE items.id = weapons.id) as parent FROM weapons WHERE parent = '543be6564bdc2df4348b4568' AND BackgroundColor IS NOT NULL AND Rarity IS NOT 'Not_exist' ORDER BY ShortName")
    fun getGrenades(): Flow<List<Weapon>>

    @Transaction
    @Query("SELECT *, (SELECT pricing FROM pricing_table WHERE pricing_table.id = weapons.id) AS pricing, (SELECT parent FROM items WHERE items.id = weapons.id) as parent FROM weapons WHERE parent = '5447e1d04bdc2dff2f8b4567' AND BackgroundColor IS NOT NULL AND Rarity IS NOT 'Not_exist' ORDER BY ShortName")
    fun getMelee(): Flow<List<Weapon>>

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
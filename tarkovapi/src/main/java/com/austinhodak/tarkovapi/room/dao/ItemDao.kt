package com.austinhodak.tarkovapi.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.austinhodak.tarkovapi.room.models.Ammo
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.room.models.PriceItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items ORDER BY name")
    suspend fun getAll(): List<Item>

    @Query("SELECT * FROM items ORDER BY name")
    fun getAllLive(): LiveData<List<Item>>

    @Query("SELECT * FROM items WHERE id = :id")
    fun getByID(id: String): Item

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Item)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Ammo)

    @Query("SELECT * FROM itemPrices")
    fun getAllPricesFlow(): Flow<List<PriceItem>>

    /*@Query("UPDATE items SET pricing = :tt WHERE id = :id")
    suspend fun updateItemsTable(id: String, tt: ItemFragment)

    @Query("UPDATE ammo SET pricing = :tt WHERE id = :id")
    suspend fun updateAmmoTable(id: String, tt: ItemFragment)

    @Query("UPDATE weapons SET pricing = :tt WHERE id = :id")
    suspend fun updateWeaponTable(id: String, tt: ItemFragment)

    @Transaction
    suspend fun updateAllPricing(id: String?, pricing: ItemFragment) {
        if (id != null) {
            updateItemsTable(id, pricing)
            updateAmmoTable(id, pricing)
            updateWeaponTable(id, pricing)
        }
    }*/

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PriceItem)

    /*//TESTING
    @Transaction
    @Query("SELECT * FROM items")
    fun getAllItemsWithPrices(): List<ItemWithPriceItem>

    @Transaction
    @Query("SELECT * FROM items WHERE id IS :id")
    fun getAllItemsWithPrices(id: String): LiveData<ItemWithPriceItem>*/


}
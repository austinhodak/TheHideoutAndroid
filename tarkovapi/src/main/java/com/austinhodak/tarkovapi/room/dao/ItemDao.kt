package com.austinhodak.tarkovapi.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.austinhodak.tarkovapi.room.enums.ItemType
import com.austinhodak.tarkovapi.room.models.Ammo
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.room.models.Pricing
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Query("SELECT * FROM items WHERE id = :id")
    fun getByID(id: String): LiveData<Item>

    @Query("SELECT * FROM items WHERE itemType = :type")
    fun getByType(type: ItemType): Flow<List<Item>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Item)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Ammo)

    @Query("UPDATE items SET pricing = :tt WHERE id = :id")
    suspend fun updateItemsTable(id: String, tt: Pricing)

    @Query("UPDATE ammo SET pricing = :tt WHERE id = :id")
    suspend fun updateAmmoTable(id: String, tt: Pricing)

    @Query("UPDATE weapons SET pricing = :tt WHERE id = :id")
    suspend fun updateWeaponTable(id: String, tt: Pricing)

    @Transaction
    suspend fun updateAllPricing(id: String?, pricing: Pricing) {
        if (id != null) {
            updateItemsTable(id, pricing)
            updateAmmoTable(id, pricing)
            updateWeaponTable(id, pricing)
        }
    }

}
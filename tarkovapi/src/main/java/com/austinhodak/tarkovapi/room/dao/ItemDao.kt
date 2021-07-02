package com.austinhodak.tarkovapi.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.austinhodak.tarkovapi.fragment.ItemFragment
import com.austinhodak.tarkovapi.room.models.AmmoItem
import com.austinhodak.tarkovapi.room.models.Item

@Dao
interface ItemDao {
    @Query("SELECT * FROM items WHERE pricing IS NOT NULL")
    fun getAll(): LiveData<List<Item>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Item)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: AmmoItem)

    @Query("UPDATE items SET pricing = :tt WHERE id = :id")
    suspend fun updateItemsTable(id: String, tt: ItemFragment)

    @Query("UPDATE ammo SET pricing = :tt WHERE id = :id")
    suspend fun updateAmmoTable(id: String, tt: ItemFragment)

    @Query("UPDATE weapons SET pricing = :tt WHERE id = :id")
    suspend fun updateWeaponTable(id: String, tt: ItemFragment)

    @Transaction
    suspend fun updateAllPricing(id: String, pricing: ItemFragment) {
        updateItemsTable(id, pricing)
        updateAmmoTable(id, pricing)
        updateWeaponTable(id, pricing)
    }
}
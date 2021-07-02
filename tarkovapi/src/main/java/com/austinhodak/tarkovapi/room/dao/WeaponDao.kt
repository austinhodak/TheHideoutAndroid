package com.austinhodak.tarkovapi.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.austinhodak.tarkovapi.room.models.AmmoItem
import com.austinhodak.tarkovapi.room.models.WeaponItem

@Dao
interface WeaponDao {

    @Query("SELECT * FROM ammo")
    fun getAllAmmo(): LiveData<List<AmmoItem>>

    @Query("SELECT * FROM ammo WHERE id = :id")
    fun getAmmoByID(id: String): AmmoItem

    @Query("SELECT * FROM ammo WHERE Caliber = :caliber")
    suspend fun getAmmoByCaliber(caliber: String): List<AmmoItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: WeaponItem)
}
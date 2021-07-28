package com.austinhodak.tarkovapi.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.austinhodak.tarkovapi.room.models.Ammo
import com.austinhodak.tarkovapi.room.models.Weapon

@Dao
interface WeaponDao {

    @Query("SELECT * FROM ammo")
    fun getAllAmmo(): LiveData<List<Ammo>>

    @Query("SELECT * FROM ammo WHERE id = :id")
    fun getAmmoByID(id: String): Ammo

    @Query("SELECT * FROM ammo WHERE Caliber = :caliber")
    suspend fun getAmmoByCaliber(caliber: String): List<Ammo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Weapon)
}
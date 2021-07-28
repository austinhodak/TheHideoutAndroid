package com.austinhodak.tarkovapi.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.austinhodak.tarkovapi.room.models.Ammo

@Dao
interface AmmoDao {

    @Query("SELECT * FROM ammo")
    fun getAllAmmo(): List<Ammo>

    @Query("SELECT * FROM ammo WHERE id = :id")
    fun getAmmoByID(id: String): Ammo

    @Query("SELECT * FROM ammo WHERE Caliber = :caliber")
    fun getAmmoByCaliber(caliber: String): List<Ammo>


}
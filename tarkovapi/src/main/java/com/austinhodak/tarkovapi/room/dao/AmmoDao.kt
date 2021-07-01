package com.austinhodak.tarkovapi.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.austinhodak.tarkovapi.room.models.AmmoItem

@Dao
interface AmmoDao {

    @Query("SELECT * FROM ammo")
    fun getAllAmmo(): List<AmmoItem>

    @Query("SELECT * FROM ammo WHERE id = :id")
    fun getAmmoByID(id: String): AmmoItem

    @Query("SELECT * FROM ammo WHERE Caliber = :caliber")
    fun getAmmoByCaliber(caliber: String): List<AmmoItem>


}
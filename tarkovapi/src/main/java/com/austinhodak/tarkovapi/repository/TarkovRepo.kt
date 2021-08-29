package com.austinhodak.tarkovapi.repository

import com.austinhodak.tarkovapi.room.dao.AmmoDao
import com.austinhodak.tarkovapi.room.dao.ItemDao
import com.austinhodak.tarkovapi.room.dao.WeaponDao
import com.austinhodak.tarkovapi.room.enums.ItemType
import com.austinhodak.tarkovapi.room.models.Ammo
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.room.models.Weapon
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TarkovRepo @Inject constructor(
    private val ammoDao: AmmoDao,
    private val itemDao: ItemDao,
    private val weaponDao: WeaponDao,
) {
    val getAllAmmo: Flow<List<Ammo>> get() = ammoDao.getAllAmmo()
    fun getAmmoByID(id: String): Flow<Ammo> = ammoDao.getAmmo(id)

    fun getItemsByType(type: ItemType): Flow<List<Item>> = itemDao.getByType(type)

    fun getWeaponsByClass(classID: String): Flow<List<Weapon>> = weaponDao.getWeaponsByClass(classID)
    fun getWeaponByID(id: String): Flow<Weapon> = weaponDao.getWeapon(id)
}
package com.austinhodak.tarkovapi.repository

import com.austinhodak.tarkovapi.room.dao.*
import com.austinhodak.tarkovapi.room.enums.ItemTypes
import com.austinhodak.tarkovapi.room.models.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TarkovRepo @Inject constructor(
    private val ammoDao: AmmoDao,
    private val itemDao: ItemDao,
    private val weaponDao: WeaponDao,
    private val questDao: QuestDao,
    private val barterDao: BarterDao,
    private val craftDao: CraftDao,
    private val priceDao: PriceDao
) {
    fun getQuestDao() = questDao
    fun getBarterDao() = barterDao
    fun getCraftDao() = craftDao
    fun getItemDao() = itemDao
    fun getPriceDao() = priceDao

    fun isPriceDaoEmpty() = priceDao.count() == 0

    val getAllAmmo: Flow<List<Ammo>> get() = ammoDao.getAllAmmo()
    fun getAmmoByID(id: String): Flow<Ammo> = ammoDao.getAmmo(id)

    fun getItemsByType(type: ItemTypes): Flow<List<Item>> = itemDao.getByType(type)
    suspend fun getItemsByContains(id: String): List<Item> = itemDao.getWhereContainsItem(id)

    fun getItemsByTypesArmor(type: List<ItemTypes>): Flow<List<Item>> = itemDao.getByTypesArmor(type)

    suspend fun getItemsByTypes(type: List<ItemTypes>): List<Item> = itemDao.getByTypes(type)

    fun getItemByID(id: String): Flow<Item> = itemDao.getByID(id)
    fun getItemByID(ids: List<String>): Flow<List<Item>> = itemDao.getByID(ids)

    fun getAllItems(): Flow<List<Item>> = itemDao.getAllItems()
    fun getAllItemsSlots(id: String): Flow<List<Item>> = itemDao.getAllItemsSlots(id)
    suspend fun getAllItemsOnce(): List<Item> = itemDao.getAllItemsOnce()

    fun getWeaponsByClass(classID: String): Flow<List<Weapon>> {
        return when (classID) {
            "grenade" -> return weaponDao.getGrenades()
            "melee" -> return weaponDao.getMelee()
            else -> weaponDao.getWeaponsByClass(classID)
        }
    }
    fun getWeaponByID(id: String): Flow<Weapon> = weaponDao.getWeapon(id)

    fun getQuestsWithItemID(id: String): Flow<List<Quest>> = questDao.getQuestsWithItemID(id)
    fun getBartersWithItemID(id: String): Flow<List<Barter>> = barterDao.getBartersWithItemID(id)
    fun getCraftsWithItemID(id: String): Flow<List<Craft>> = craftDao.getCraftsWithItemID(id)

    fun getAllBarters(): Flow<List<Barter>> = barterDao.getAllBarters()

    fun getQuestsWithObjectiveID(id: String): Flow<Quest> = questDao.getQuestWithObjectiveID(id)

    fun getAllCrafts(): Flow<List<Craft>> = craftDao.getAllCrafts()

    fun isQuestRequiredForKappa(id: String): Flow<Int> = questDao.isQuestRequiredForKappa(id)

    fun numOfQuests(): Flow<Int> = questDao.numOfQuests()
    fun getAllQuests(): Flow<List<Quest>> = questDao.getAlLQuests()
    suspend fun getAllQuestsOnce(): List<Quest> = questDao.getAlLQuestsOnce()
    fun getQuestByID(id: String): Flow<Quest> = questDao.getQuestByID(id)

    fun getAllWeapons(): Flow<List<Weapon>> = weaponDao.getAllWeapons()

}
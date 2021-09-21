package com.austinhodak.tarkovapi.repository

import com.austinhodak.tarkovapi.room.dao.*
import com.austinhodak.tarkovapi.room.models.Mod
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ModsRepo @Inject constructor(
    private val modDao: ModDao
) {

    fun getAllMods(): Flow<List<Mod>> = modDao.getAllMods()
    fun getModByID(id: String): Flow<Mod> = modDao.getByID(id)
    fun getModBySlot(id: String): Flow<List<Mod>> = modDao.getModsForSlot(id)
    fun getModsByIDs(ids: List<String>): Flow<List<Mod>> = modDao.getByID(ids)
    fun getModByParent(id: String): Flow<List<Mod>> = modDao.getByParent(id)
    fun getModByParent(id: List<String>): Flow<List<Mod>> = modDao.getByParent(id)

}
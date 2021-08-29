package com.austinhodak.tarkovapi.room

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.apollographql.apollo3.ApolloClient
import com.austinhodak.tarkovapi.R
import com.austinhodak.tarkovapi.di.ApplicationScope
import com.austinhodak.tarkovapi.room.dao.AmmoDao
import com.austinhodak.tarkovapi.room.dao.ItemDao
import com.austinhodak.tarkovapi.room.dao.QuestDao
import com.austinhodak.tarkovapi.room.dao.WeaponDao
import com.austinhodak.tarkovapi.room.enums.ItemType
import com.austinhodak.tarkovapi.room.models.*
import com.austinhodak.tarkovapi.utils.itemType
import com.austinhodak.tarkovapi.utils.toPricing
import com.austinhodak.tarkovapi.utils.toQuest
import com.austinhodak.thehideout.ItemsByTypeQuery
import com.austinhodak.thehideout.QuestsQuery
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Ammo::class, Item::class, Weapon::class, Quest::class], version = 15)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun AmmoDao(): AmmoDao
    abstract fun ItemDao(): ItemDao
    abstract fun WeaponDao(): WeaponDao
    abstract fun QuestDao(): QuestDao

    class Callback @Inject constructor(
        @ApplicationContext private val context: Context,
        @ApplicationScope private val scope: CoroutineScope,
        private val database: Provider<AppDatabase>,
        private val apolloClient: ApolloClient
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            loadItemsFile()
        }

        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
            super.onDestructiveMigration(db)
            loadItemsFile()
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            scope.launch(Dispatchers.IO) {
                updatePricing()
            }
        }

        private fun loadItemsFile() {
            scope.launch(Dispatchers.IO) {
                populateDatabase(JSONArray(context.resources.openRawResource(R.raw.items_082521).bufferedReader().use { it.readText() }))
            }
        }

        private suspend fun populateDatabase(jsonArray: JSONArray) {
            val ammoDao = database.get().AmmoDao()
            val itemDao = database.get().ItemDao()
            val weaponDao = database.get().WeaponDao()
            val questDao = database.get().QuestDao()

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                when (item.itemType()) {
                    ItemType.AMMO -> ammoDao.insert(item.toAmmoItem())
                    ItemType.WEAPON -> {
                        val weapon = item.getJSONObject("_props").toWeapon(item.getString("_id"))
                        weaponDao.insert(weapon)
                    }
                    else -> {
                    }
                }

                if (item.itemType() != ItemType.NULL)
                    itemDao.insert(item.toItem())
            }

            val test = apolloClient.query(QuestsQuery())
            val quests = test.data?.quests
            Gson().toJson(quests)

            updatePricing()
            populateQuests()
        }

        private suspend fun populateQuests() {
            val questDao = database.get().QuestDao()
            val response = apolloClient.query(QuestsQuery())
            val quests = response.data?.quests?.map { quest ->
                quest?.toQuest()
            } ?: emptyList()
            for (quest in quests) {
                if (quest != null) {
                    questDao.insert(quest)
                }
            }
        }

        private suspend fun updatePricing() {
            val itemDao = database.get().ItemDao()
            val response = apolloClient.query(ItemsByTypeQuery(com.austinhodak.thehideout.type.ItemType.any))
            val items = response.data?.itemsByType?.map { fragments ->
                fragments?.toPricing()
            } ?: emptyList()
            for (item in items) {
                if (item != null)
                    itemDao.updateAllPricing(item.id, item)
            }
        }
    }
}
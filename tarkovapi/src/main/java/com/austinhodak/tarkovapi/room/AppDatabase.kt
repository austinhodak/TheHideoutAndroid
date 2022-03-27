package com.austinhodak.tarkovapi.room

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.apollographql.apollo3.ApolloClient
import com.austinhodak.tarkovapi.*
import com.austinhodak.tarkovapi.di.ApplicationScope
import com.austinhodak.tarkovapi.room.dao.*
import com.austinhodak.tarkovapi.room.enums.ItemTypes
import com.austinhodak.tarkovapi.room.models.*
import com.austinhodak.tarkovapi.type.ItemType
import com.austinhodak.tarkovapi.utils.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import org.json.JSONArray
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider
import kotlin.system.measureTimeMillis

@Database(entities = [Ammo::class, Item::class, Weapon::class, Quest::class, Trader::class, Craft::class, Barter::class, Mod::class, Price::class], version = 51)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun AmmoDao(): AmmoDao
    abstract fun ItemDao(): ItemDao
    abstract fun WeaponDao(): WeaponDao
    abstract fun QuestDao(): QuestDao
    abstract fun TraderDao(): TraderDao
    abstract fun BarterDao(): BarterDao
    abstract fun CraftDao(): CraftDao
    abstract fun ModDao(): ModDao
    abstract fun PriceDao(): PriceDao

    class Callback @Inject constructor(
        @ApplicationContext private val context: Context,
        @ApplicationScope private val scope: CoroutineScope,
        private val database: Provider<AppDatabase>,
        private val apolloClient: ApolloClient
    ) : RoomDatabase.Callback() {
        private val preferences = context.getSharedPreferences("tarkov", MODE_PRIVATE)

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            loadItemsFile()
        }

        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
            super.onDestructiveMigration(db)
            preferences.edit().putLong("lastPriceUpdate", 0).apply()
            loadItemsFile()
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            scope.launch(Dispatchers.IO) {
                //updatePricing()
            }
        }

        private fun loadItemsFile() {
            scope.launch(Dispatchers.IO) {
                populateDatabase(JSONArray(context.resources.openRawResource(R.raw.items_011922).bufferedReader().use { it.readText() }))
            }
        }

        private suspend fun populateDatabase(jsonArray: JSONArray) {
            Timber.d("Populating Database")
            val ammoDao = database.get().AmmoDao()
            val itemDao = database.get().ItemDao()
            val weaponDao = database.get().WeaponDao()
            val modDao = database.get().ModDao()

            val ms = measureTimeMillis {
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)
                    when (item.itemType()) {
                        ItemTypes.AMMO -> ammoDao.insert(item.toAmmoItem())
                        ItemTypes.WEAPON -> {
                            val weapon = item.getJSONObject("_props").toWeapon(item.getString("_id"))
                            weaponDao.insert(weapon)
                        }
                        ItemTypes.MOD -> {
                            modDao.insert(item.toMod())
                        }
                        else -> {

                        }
                    }

                    if (item.itemType() != ItemTypes.NULL)
                        Timber.d("Item $i")
                    itemDao.insert(item.toItem())
                }
            }

            Timber.d("Database populated in $ms ms")
        }
    }
}
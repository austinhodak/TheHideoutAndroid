package com.austinhodak.tarkovapi.room

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.apollographql.apollo3.ApolloClient
import com.austinhodak.tarkovapi.*
import com.austinhodak.tarkovapi.di.ApplicationScope
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.dao.*
import com.austinhodak.tarkovapi.room.enums.ItemTypes
import com.austinhodak.tarkovapi.room.models.*
import com.austinhodak.tarkovapi.utils.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider
import kotlin.system.measureTimeMillis

@Database(entities = [Ammo::class, Item::class, Weapon::class, Quest::class, Trader::class, Craft::class, Barter::class, Mod::class, Price::class], version = 59)
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
                loadItemsFile()
            }
        }

        private fun loadItemsFile() {
            scope.launch(Dispatchers.IO) {
                populateDatabase(JSONArray(context.resources.openRawResource(R.raw.items_041322).bufferedReader().use { it.readText() }))
            }
        }

        private suspend fun populateDatabase(jsonArray: JSONArray) {
            Timber.d("Populating Database")
            val ammoDao = database.get().AmmoDao()
            val itemDao = database.get().ItemDao()
            val weaponDao = database.get().WeaponDao()
            val modDao = database.get().ModDao()

            val ms = measureTimeMillis {
                val items: MutableList<Item> = mutableListOf()
                val ammo: MutableList<Ammo> = mutableListOf()
                val mods: MutableList<Mod> = mutableListOf()
                val weapons: MutableList<Weapon> = mutableListOf()
                for (item in jsonArray.iterator<JSONObject>()) {
                    when (item.getItemType()) {
                        ItemTypes.AMMO -> {
                            if (item.itemType() == ItemTypes.NULL) continue
                            ammo.add(item.toAmmoItem())
                        }
                        ItemTypes.GRENADE,
                        ItemTypes.MELEE,
                        ItemTypes.WEAPON -> {
                            val weapon = item.getJSONObject("_props").toWeapon(item.getString("_id"))
                            weapons.add(weapon)
                        }
                        ItemTypes.MOD -> {
                            mods.add(item.toMod())
                        }
                        else -> {

                        }
                    }

                    items.add(item.toItem())

                    Timber.d("Item: ${item.getString("_id")}")
                }

                itemDao.insertAll(items)
                ammoDao.insertAll(ammo)
                weaponDao.insertAll(weapons)
                modDao.insertAll(mods)
            }

            Timber.d("Database populated in $ms ms")

            //Updaters(tarkovRepo, apolloClient).updateAll()
        }
    }
}
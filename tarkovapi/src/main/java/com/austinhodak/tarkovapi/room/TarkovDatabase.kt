package com.austinhodak.tarkovapi.room

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.austinhodak.tarkovapi.ItemsByTypeQuery
import com.austinhodak.tarkovapi.R
import com.austinhodak.tarkovapi.room.dao.*
import com.austinhodak.tarkovapi.room.models.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray

@Database(entities = [Item::class, AmmoItem::class, WeaponItem::class, Craft::class, Barter::class, Quest::class], version = 15)
@TypeConverters(Converters::class)
abstract class TarkovDatabase : RoomDatabase() {
    abstract fun ItemDao(): ItemDao
    abstract fun WeaponDao(): WeaponDao
    abstract fun CraftDao(): CraftDao
    abstract fun BarterDao(): BarterDao
    abstract fun QuestDao(): QuestDao

    companion object {
        @Volatile
        private var INSTANCE: TarkovDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): TarkovDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TarkovDatabase::class.java,
                    "item_database"
                ).fallbackToDestructiveMigration().addCallback(DatabaseCallback(context, scope)).build()

                Log.d("DATABASE", "RETURN")
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context,
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                Log.d("DATABASE", "OPENED")

                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        JSONArray(context.resources.openRawResource(R.raw.bsg_items_array).bufferedReader().use { it.readText() }).let {
                            populateDatabase(INSTANCE!!, it, scope)
                        }
                    }
                }
            }

            override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                super.onDestructiveMigration(db)
                Log.d("DATABASE", "OPENED")

                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        JSONArray(context.resources.openRawResource(R.raw.bsg_items_array).bufferedReader().use { it.readText() }).let {
                            populateDatabase(INSTANCE!!, it, scope)
                        }
                    }
                }
            }
        }

        fun populateDatabase(database: TarkovDatabase, items: JSONArray, scope: CoroutineScope) {
            val itemDao = database.ItemDao()
            val weaponDao = database.WeaponDao()
            val craftDao = database.CraftDao()

            for (i in 0 until items.length()) {
                val item = items.getJSONObject(i)
                val props = item.getJSONObject("_props")
                scope.launch(Dispatchers.IO) {

                    when {
                        props.has("weapFireType") -> weaponDao.insert(toWeaponItem(item))
                        props.has("Prefab") && props.getJSONObject("Prefab").getString("path").contains("assets/content/items/mods") -> ItemType.MODS
                        props.has("Caliber") -> itemDao.insert(toAmmoItem(item))
                        else -> {

                        }
                    }

                    if (props.optString("Name").isNullOrEmpty()) {
                        return@launch
                    }

                    itemDao.insert(
                        toItem(item)
                    )
                }
            }
        }
    }

    fun updatePricing(api: ApolloClient, scope: CoroutineScope) {
        scope.launch {
            val response = api.query(ItemsByTypeQuery(com.austinhodak.tarkovapi.type.ItemType.ANY)).responseFetcher(ApolloResponseFetchers.NETWORK_ONLY).await()
            scope.launch(Dispatchers.IO) {
                for (i in response.data?.itemsByType ?: emptyList()) {
                    val item = i?.fragments?.itemFragment
                    if (item?.id != null) {
                        //Timber.d("UPDATING ROOM: ${item.id}")
                        ItemDao().updateAllPricing(item.id, item)
                    }
                }
            }
        }
    }
}
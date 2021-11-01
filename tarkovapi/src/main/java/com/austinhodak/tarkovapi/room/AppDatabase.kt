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
import com.austinhodak.tarkovapi.models.QuestExtra
import com.austinhodak.tarkovapi.room.dao.*
import com.austinhodak.tarkovapi.room.enums.ItemTypes
import com.austinhodak.tarkovapi.room.models.*
import com.austinhodak.tarkovapi.type.ItemType
import com.austinhodak.tarkovapi.utils.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import timber.log.Timber
import java.lang.reflect.Type
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Ammo::class, Item::class, Weapon::class, Quest::class, Trader::class, Craft::class, Barter::class, Mod::class], version = 43)
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
            //setupTraders()
        }

        private fun setupTraders() {
            val traderDao = database.get().TraderDao()

            scope.launch(Dispatchers.IO) {
                Traders.values().forEach {
                    traderDao.insert(
                        Trader(it.id, 1)
                    )
                }
            }
        }

        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
            super.onDestructiveMigration(db)
            preferences.edit().putLong("lastPriceUpdate", 0).apply()
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
                populateDatabase(JSONArray(context.resources.openRawResource(R.raw.items_110121).bufferedReader().use { it.readText() }))
            }
        }

        private suspend fun populateDatabase(jsonArray: JSONArray) {
            val ammoDao = database.get().AmmoDao()
            val itemDao = database.get().ItemDao()
            val weaponDao = database.get().WeaponDao()
            val modDao = database.get().ModDao()

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
                    itemDao.insert(item.toItem())
            }

            try {
                updatePricing()
                populateQuests()
                populateCrafts()
                populateBarters()
            } catch (e: Exception) {

            }
        }

        private suspend fun populateQuests() {
            val questDao = database.get().QuestDao()
            val response = apolloClient.query(QuestsQuery())
            //val questsJSON = getJsonDataFromAsset(context, R.raw.quests)
            //val questType: Type = object : TypeToken<ArrayList<QuestExtra.QuestExtraItem?>?>() {}.type

            //val questsExtraData: List<QuestExtra.QuestExtraItem> = Gson().fromJson(questsJSON, questType)

            val quests = response.data?.quests?.map { quest ->
                //val questExtra = questsExtraData.find { it.id.toString() == quest?.fragments?.questFragment?.id }
                quest?.toQuest(null)
            } ?: emptyList()

            for (quest in quests) {
                if (quest != null) {
                    questDao.insert(quest)
                }
            }
        }

        private suspend fun populateCrafts() {
            val craftDao = database.get().CraftDao()
            val response = apolloClient.query(CraftsQuery())
            val crafts = response.data?.crafts?.map { craft ->
                craft?.toCraft()
            } ?: emptyList()
            for (craft in crafts) {
                if (craft != null) {
                    craftDao.insert(craft)
                }
            }
        }

        private suspend fun populateBarters() {
            val barterDao = database.get().BarterDao()
            val response = apolloClient.query(BartersQuery())
            val barters = response.data?.barters?.map { barter ->
                barter?.toBarter()
            } ?: emptyList()
            for (barter in barters) {
                if (barter != null) {
                    barterDao.insert(barter)
                }
            }
        }

        private suspend fun updatePricing() {
            val oneHour = 1000 * 60 * 60
            if (preferences.getLong("lastPriceUpdate", 0) + oneHour > System.currentTimeMillis()) {
                return
            }

            try {
                val itemDao = database.get().ItemDao()
                val response = apolloClient.query(ItemsByTypeQuery(ItemType.any))
                val items = response.data?.itemsByType?.map { fragments ->
                    fragments?.toPricing()
                } ?: emptyList()

                //val itemsChunked = items.chunked(900)

                for (item in items) {
                    Timber.d("UPDATE PRICING")
                    if (item != null)
                        itemDao.updateAllPricing(item.id, item)
                }

                preferences.edit().putLong("lastPriceUpdate", System.currentTimeMillis()).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    enum class Traders (var id: String) {
        PRAPOR      ("Prapor"),
        THERAPIST   ("Therapist"),
        FENCE       ("Fence"),
        SKIER       ("Skier"),
        PEACEKEEPER ("Peacekeeper"),
        MECHANIC    ("Mechanic"),
        RAGMAN      ("Ragman"),
        JAEGER      ("Jaeger"),
    }
}
package com.austinhodak.thehideout.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.austinhodak.thehideout.room.entities.MarketItem

@Database(entities = [MarketItem::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

}
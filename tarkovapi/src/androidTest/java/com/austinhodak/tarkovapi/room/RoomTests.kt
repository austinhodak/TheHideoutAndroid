package com.austinhodak.tarkovapi.room

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.austinhodak.tarkovapi.TestUtil
import com.austinhodak.tarkovapi.room.dao.ItemDao
import com.austinhodak.tarkovapi.room.models.Item
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ItemReadWriteTest {
    private lateinit var itemDao: ItemDao
    //private lateinit var db: TarkovDatabase

    /*@Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, TarkovDatabase::class.java).build()
        itemDao = db.ItemDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeUserAndReadInList() {
        //val item: Item = TestUtil.createItem()
        *//*runBlocking {
            itemDao.insert(item)
        }

        val byName = itemDao.getByID("590c657e86f77412b013051d")
        assertThat(byName, equalTo(item))*//*
    }*/
}
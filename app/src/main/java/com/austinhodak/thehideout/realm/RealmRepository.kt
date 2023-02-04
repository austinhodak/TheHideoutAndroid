package com.austinhodak.thehideout.realm

import com.austinhodak.thehideout.data.enums.ItemType
import com.austinhodak.thehideout.realm.models.Ammo
import com.austinhodak.thehideout.realm.models.Item
import com.austinhodak.thehideout.realm.models.Task
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealmRepository @Inject constructor(
    private val realm: Realm
) {
    fun findItemByID(id: String): Item? {
        Timber.tag("Find Item By ID").d("Finding item with id: $id")
        return realm.query<Item>("id == $0", id).first().find()
    }

    fun findAllItems() = realm.query<Item>().limit(1000).asFlow()

    fun findItemsByType(type: Any) = realm.query<Item>().apply {
        when (type) {
            is ItemType -> query("types IN $0", listOf(type.rawValue))
            is String -> query("types IN $0", listOf(type))
        }
    }.asFlow()


    fun getAllAmmo() = realm.query<Ammo>().asFlow()

    fun getAllTasks() = realm.query<Task>("name CONTAINS[c] 'pharmacist'").limit(30).asFlow()
    fun getTaskByID(id: String) = realm.query<Task>("id == $0", id).first().find()
}
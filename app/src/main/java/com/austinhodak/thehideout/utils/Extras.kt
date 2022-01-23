package com.austinhodak.thehideout.utils

import android.content.Context
import android.content.SharedPreferences
import com.austinhodak.tarkovapi.R
import com.austinhodak.tarkovapi.models.AmmoBallistic
import com.austinhodak.tarkovapi.models.Server
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import java.lang.reflect.Type

private const val FAVORITE_ITEMS = "FAVORITE_ITEMS"
private const val OPENING_PAGE = "OPENING_PAGE_N"
private const val OPENING_PAGE_TAG = "OPENING_PAGE_TAG_N"

class Extras(context: Context) {

    var servers: MutableList<Server> = mutableListOf()

    init {
        loadServers(context)
    }

    private fun loadServers(context: Context) {
        if (servers.isNullOrEmpty()) {
            val groupListType: Type = object : TypeToken<ArrayList<Server?>?>() {}.type
            servers = Gson().fromJson(context.resources.openRawResource(R.raw.ip).bufferedReader().use { it.readText() }, groupListType)
        }
    }

    val preference: SharedPreferences = context.getSharedPreferences("hideout", Context.MODE_PRIVATE)

    var favoriteItems: MutableSet<String>?
        get() = preference.getStringSet(FAVORITE_ITEMS, emptySet())
        set(value) = preference.edit().putStringSet(FAVORITE_ITEMS, value).apply()

    var openingPage: Long
        get() = preference.getLong(OPENING_PAGE, 107)
        set(value) = preference.edit().putLong(OPENING_PAGE, value).apply()

    var openingPageTag: String
        get() = preference.getString(OPENING_PAGE_TAG, "flea") ?: "flea"
        set(value) = preference.edit().putString(OPENING_PAGE_TAG, value).apply()

    fun setOpeningItem(item: IDrawerItem<*>) {
        when (item.identifier.toInt()) {
            101 -> {
                openingPage = item.identifier
                openingPageTag = "ammunition/{caliber}"
            }
            else -> {
                openingPage = item.identifier
                openingPageTag = item.tag.toString()
            }
        }
    }

    fun setOpeningItem(identifier: Int, tag: String) {
        when (identifier) {
            101 -> {
                openingPage = identifier.toLong()
                openingPageTag = tag
            }
            else -> {
                openingPage = identifier.toLong()
                openingPageTag = tag
            }
        }
    }

    fun addFavorite(id: String) {
        val set = mutableSetOf<String>()
        if (favoriteItems != null)
            set.addAll(favoriteItems!!)
        set.add(id)
        favoriteItems = set
    }

    fun removeFavorite(id: String) {
        val set = mutableSetOf<String>()
        if (favoriteItems != null)
            set.addAll(favoriteItems!!)
        set.remove(id)
        favoriteItems = set
    }


}
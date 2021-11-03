package com.austinhodak.thehideout.utils

import android.content.Context
import android.content.SharedPreferences
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem

private const val FAVORITE_ITEMS = "FAVORITE_ITEMS"
private const val OPENING_PAGE = "OPENING_PAGE_N"
private const val OPENING_PAGE_TAG = "OPENING_PAGE_TAG_N"

class Prefs(context: Context) {

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
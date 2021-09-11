package com.austinhodak.thehideout.utils

import android.content.Context
import android.content.SharedPreferences
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem

private const val FAVORITE_ITEMS = "FAVORITE_ITEMS"
private const val OPENING_PAGE = "OPENING_PAGE"
private const val OPENING_PAGE_TAG = "OPENING_PAGE_TAG"

class Prefs(context: Context) {

    val preference: SharedPreferences = context.getSharedPreferences("hideout", Context.MODE_PRIVATE)

    var favoriteItems: MutableSet<String>?
        get() = preference.getStringSet(FAVORITE_ITEMS, emptySet())
        set(value) = preference.edit().putStringSet(FAVORITE_ITEMS, value).apply()

    var openingPage: Long
        get() = preference.getLong(OPENING_PAGE, 104)
        set(value) = preference.edit().putLong(OPENING_PAGE, value).apply()

    var openingPageTag: String
        get() = preference.getString(OPENING_PAGE_TAG, "keys") ?: "keys"
        set(value) = preference.edit().putString(OPENING_PAGE_TAG, value).apply()

    fun setOpeningItem(item: IDrawerItem<*>) {
        openingPage = item.identifier
        openingPageTag = item.tag.toString()
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
package com.austinhodak.thehideout.utils

import android.content.Context
import android.content.SharedPreferences

private const val FAVORITE_ITEMS = "FAVORITE_ITEMS"

class Prefs(context: Context) {

    val preference: SharedPreferences = context.getSharedPreferences("hideout", Context.MODE_PRIVATE)

    var favoriteItems: MutableSet<String>?
        get() = preference.getStringSet(FAVORITE_ITEMS, emptySet())
        set(value) = preference.edit().putStringSet(FAVORITE_ITEMS, value).apply()

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
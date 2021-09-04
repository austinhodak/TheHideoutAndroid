package com.austinhodak.thehideout

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class NavViewModel @Inject constructor() : ViewModel() {

    private val _isDrawerOpen = MutableLiveData(false)
    val isDrawerOpen = _isDrawerOpen

    fun setDrawerOpen(isOpen: Boolean) {
        isDrawerOpen.value = isOpen
    }

    private val _selectedDrawerItem = MutableLiveData(Pair("quests", 101))
    val selectedDrawerItem = _selectedDrawerItem

    fun drawerItemSelected(route: Pair<String, Int>) {
        _selectedDrawerItem.value = route
    }

    private val _isSearchOpen = MutableLiveData(false)
    val isSearchOpen = _isSearchOpen

    fun setSearchOpen(isOpen: Boolean) {
        isSearchOpen.value = isOpen
    }

    init {
        Timber.d("Init")
    }

}
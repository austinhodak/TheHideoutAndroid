package com.austinhodak.thehideout

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
open class SearchViewModel @Inject constructor(

) : ViewModel() {

    private val _isSearchOpen = MutableLiveData(false)
    val isSearchOpen = _isSearchOpen

    fun setSearchOpen(isOpen: Boolean) {
        isSearchOpen.value = isOpen
    }

    private val _searchKey = MutableLiveData("")
    val searchKey = _searchKey

    fun setSearchKey(string: String) {
        _searchKey.value = string
    }

    fun clearSearch() {
        _searchKey.value = ""
    }

}
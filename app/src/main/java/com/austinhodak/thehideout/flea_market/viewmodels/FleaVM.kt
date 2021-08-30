package com.austinhodak.thehideout.flea_market.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.austinhodak.tarkovapi.ItemQuery
import com.austinhodak.tarkovapi.ItemsByTypeQuery
import com.austinhodak.tarkovapi.repository.TarkovRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class FleaVM @Inject constructor(
    @ApplicationContext application: Context,
    private val tarkovRepo: TarkovRepo
) : ViewModel() {

    private val _itemsList by lazy { MutableLiveData<ItemsByTypeQuery.Data>() }
    val itemsList: LiveData<ItemsByTypeQuery.Data> get() = _itemsList

    private val _item by lazy { MutableLiveData<ItemQuery.Data>() }
    val item: LiveData<ItemQuery.Data> get() = _item

    fun getItemByID(id: String) = viewModelScope.launch {

    }

}
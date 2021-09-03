package com.austinhodak.thehideout.flea_market.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.models.Item
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class FleaVM @Inject constructor(
    @ApplicationContext application: Context,
    private val tarkovRepo: TarkovRepo
) : ViewModel() {

    private val _item by lazy { MutableLiveData<Item>() }
    val item: LiveData<Item> get() = _item

    fun getItemByID(id: String) = viewModelScope.launch {
        tarkovRepo.getItemByID(id).collect {
            _item.value = it
        }
    }

    var sortBy = MutableLiveData(1)

    fun setSort(int: Int) {
        sortBy.value = int
    }

}
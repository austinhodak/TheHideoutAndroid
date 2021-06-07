package com.austinhodak.thehideout.flea_market.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.austinhodak.tarkovapi.ItemsByTypeQuery
import com.austinhodak.tarkovapi.repository.TarkovRepository
import com.austinhodak.tarkovapi.view.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class FleaVM @Inject constructor(
    private val repository: TarkovRepository
) : ViewModel() {

    private val _itemsList by lazy { MutableLiveData<ViewState<Response<ItemsByTypeQuery.Data>>>() }
    val itemsList: LiveData<ViewState<Response<ItemsByTypeQuery.Data>>> get() = _itemsList

    fun queryItemsList() = viewModelScope.launch {
        _itemsList.postValue(ViewState.Loading())
        try {
            val response = repository.queryItems()
            _itemsList.postValue(ViewState.Success(response))
        } catch (e: ApolloException) {
            Log.d("ApolloException", "Failure", e)
            _itemsList.postValue(ViewState.Error("Error fetching items."))
        }
    }

    init {
        queryItemsList()
    }

}
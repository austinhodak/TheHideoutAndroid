package com.austinhodak.thehideout.flea_market.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.coroutines.toFlow
import com.apollographql.apollo.exception.ApolloException
import com.austinhodak.tarkovapi.ItemQuery
import com.austinhodak.tarkovapi.ItemsByTypeQuery
import com.austinhodak.tarkovapi.networking.TarkovApi
import com.austinhodak.tarkovapi.repository.TarkovRepository
import com.austinhodak.tarkovapi.type.ItemType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class FleaVM @Inject constructor(
    @ApplicationContext application: Context,
    private val repository: TarkovRepository,
    private val api: TarkovApi
) : ViewModel() {

    /**/
    private val _itemsList by lazy { MutableLiveData<ItemsByTypeQuery.Data>() }
    val itemsList: LiveData<ItemsByTypeQuery.Data> get() = _itemsList

    private fun queryItemsList(type: ItemType = ItemType.ANY) = viewModelScope.launch {
        try {
            //Timber.d("Start")
            val response = repository.queryItems(type)
            _itemsList.postValue(response.data)
            //Timber.d(response.data?.itemsByType?.count().toString())
        } catch (e: ApolloException) {
            Timber.e(e)
        }
    }

    suspend fun test() = repository.queryItems(ItemType.BACKPACK)

    fun test2(id: String): Flow<Response<ItemQuery.Data>> {
        Timber.d(id)
        return api.getTarkovClient().query(ItemQuery(id)).watcher().toFlow()
    }

    /**/
    private val _item by lazy { MutableLiveData<ItemQuery.Data>() }
    val item: LiveData<ItemQuery.Data> get() = _item

    fun getItemByID(id: String) = viewModelScope.launch {
        try {
            val response = repository.queryItemByID(id)
            _item.postValue(response.data)
        } catch (e: ApolloException) {
            e.printStackTrace()
        }
    }

    /**/

    init {
        queryItemsList()
    }

}
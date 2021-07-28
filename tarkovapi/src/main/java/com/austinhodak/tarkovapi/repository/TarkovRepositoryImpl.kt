package com.austinhodak.tarkovapi.repository

import android.content.Context
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.coroutines.await
import com.austinhodak.tarkovapi.ItemQuery
import com.austinhodak.tarkovapi.ItemsByTypeQuery
import com.austinhodak.tarkovapi.networking.TarkovApi
import com.austinhodak.tarkovapi.type.ItemType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class TarkovRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: TarkovApi
) : TarkovRepository {

    override suspend fun queryItems(type: ItemType): Response<ItemsByTypeQuery.Data> {
        return api.getTarkovClient(context).query(ItemsByTypeQuery(type = type)).await()
    }

    override suspend fun queryItemByID(id: String): Response<ItemQuery.Data> {
        return api.getTarkovClient(context).query(ItemQuery(id = id)).await()
    }

}
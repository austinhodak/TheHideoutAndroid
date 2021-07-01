package com.austinhodak.tarkovapi.repository

import com.apollographql.apollo.api.Response
import com.austinhodak.tarkovapi.ItemQuery
import com.austinhodak.tarkovapi.ItemsByTypeQuery
import com.austinhodak.tarkovapi.type.ItemType

interface TarkovRepository {

    suspend fun queryItems(type: ItemType): Response<ItemsByTypeQuery.Data>
    suspend fun queryItemByID(id: String): Response<ItemQuery.Data>

}
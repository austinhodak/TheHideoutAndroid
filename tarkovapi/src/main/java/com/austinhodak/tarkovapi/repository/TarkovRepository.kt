package com.austinhodak.tarkovapi.repository

import com.apollographql.apollo.api.Response
import com.austinhodak.tarkovapi.ItemsByTypeQuery

interface TarkovRepository {

    suspend fun queryItems(): Response<ItemsByTypeQuery.Data>

}
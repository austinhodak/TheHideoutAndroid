package com.austinhodak.tarkovapi.repository

import com.apollographql.apollo.api.Response
import com.apollographql.apollo.coroutines.await
import com.austinhodak.tarkovapi.ItemsByTypeQuery
import com.austinhodak.tarkovapi.networking.TarkovApi
import javax.inject.Inject

class TarkovRepositoryImpl @Inject constructor(
    private val webService: TarkovApi
) : TarkovRepository {
    override suspend fun queryItems(): Response<ItemsByTypeQuery.Data> {
        return webService.getTarkovClient().query(ItemsByTypeQuery()).await()
    }
}
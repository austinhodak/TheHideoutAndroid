package com.austinhodak.tarkovapi.networking

import android.content.Context
import android.os.Looper
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.ResponseField
import com.apollographql.apollo.cache.normalized.CacheKey
import com.apollographql.apollo.cache.normalized.CacheKeyResolver
import com.apollographql.apollo.cache.normalized.lru.EvictionPolicy
import com.apollographql.apollo.cache.normalized.lru.LruNormalizedCacheFactory
import com.apollographql.apollo.cache.normalized.sql.SqlNormalizedCacheFactory
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import okhttp3.OkHttpClient
import java.util.*
import java.util.concurrent.TimeUnit

class TarkovApi {

    fun getTarkovClient(context: Context? = null): ApolloClient {
        check(Looper.myLooper() == Looper.getMainLooper()) {
            "Only the main thread can get the apolloClient instance"
        }

        val resolver: CacheKeyResolver = object : CacheKeyResolver() {

            //Retrieval
            override fun fromFieldRecordSet(field: ResponseField, recordSet: Map<String, Any>): CacheKey {

                val typePrefix = (recordSet["__typename"] as String).toUpperCase(Locale.ROOT)
                val id = recordSet["id"] as String?

                //Log.d("TARKOVAPI", "RETRIEVING: $typePrefix.$id")

                return when (typePrefix) {
                    "ITEM",
                    "QUESTOBJECTIVE",
                    "QUEST",
                    "TRADER" -> {
                        //Log.d("TARKOVAPI", "FOUND: $typePrefix.$id")
                        CacheKey.from("$typePrefix.$id")
                    }
                    else -> {
                        //Log.d("TARKOVAPI", "NO KEY: $typePrefix.$id")
                        CacheKey.NO_KEY
                    }
                }
            }

            //Saving
            override fun fromFieldArguments(field: ResponseField, variables: Operation.Variables): CacheKey {

                val id = field.resolveArgument("id", variables) as String
                val fullID = "${field.fieldName.toUpperCase(Locale.ROOT)}.$id"

                return when (field.fieldName.toUpperCase()) {
                    "ITEM",
                    "QUESTOBJECTIVE",
                    "QUEST",
                    "TRADER" -> {
                        //Log.d("TARKOVAPI", "SAVING: $fullID")
                        CacheKey.from(fullID)
                    }
                    else -> {
                        //Log.d("TARKOVAPI", "SAVING NO KEY: $fullID")
                        CacheKey.NO_KEY
                    }
                }
            }
        }

        val cacheFactory = LruNormalizedCacheFactory(EvictionPolicy.builder().maxSizeBytes(10 * 1024 * 1024).expireAfterAccess(1, TimeUnit.HOURS).build())

        val sqlCache = SqlNormalizedCacheFactory(context!!, "apollo")
        val inMemoryThenSqliteCache = cacheFactory.chain(sqlCache)

        //val cacheStore =  DiskLruHttpCacheStore(File(context.cacheDir, "apolloCache"), 10 * 1024 * 1024)

        val okHttpClient = OkHttpClient.Builder().build()
        return ApolloClient.builder()
            .serverUrl("https://tarkov-tools.com/graphql")
            .okHttpClient(okHttpClient)
            .normalizedCache(sqlCache, resolver)
            .defaultResponseFetcher(ApolloResponseFetchers.CACHE_FIRST)
            .build()
    }
}
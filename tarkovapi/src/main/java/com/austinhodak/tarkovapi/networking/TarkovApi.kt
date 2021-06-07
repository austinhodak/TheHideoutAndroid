package com.austinhodak.tarkovapi.networking

import android.os.Looper
import com.apollographql.apollo.ApolloClient
import okhttp3.OkHttpClient

class TarkovApi {

    fun getTarkovClient(): ApolloClient {
        check(Looper.myLooper() == Looper.getMainLooper()) {
            "Only the main thread can get the apolloClient instance"
        }

        val okHttpClient = OkHttpClient.Builder().build()
        return ApolloClient.builder()
            .serverUrl("https://tarkov-tools.com/graphql")
            .okHttpClient(okHttpClient)
            .build()
    }

}
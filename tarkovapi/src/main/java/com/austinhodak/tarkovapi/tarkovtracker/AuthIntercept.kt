package com.austinhodak.tarkovapi.tarkovtracker

import okhttp3.Credentials
import okhttp3.Interceptor

class AuthIntercept(val apiKey: String): Interceptor {

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        var request = chain.request()
        request = request.newBuilder().header("Authorization", "Bearer $apiKey").build()

        return chain.proceed(request)
    }
}
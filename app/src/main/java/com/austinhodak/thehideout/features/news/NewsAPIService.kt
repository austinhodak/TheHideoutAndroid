package com.austinhodak.thehideout.features.news

import com.austinhodak.thehideout.features.news.models.NewsItem
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

const val BASE_URL = "https://api.developertracker.com/escape-from-tarkov/"

interface NewsAPIService {
    @GET("posts")
    suspend fun getTodos(): NewsItem

    companion object {
        var apiService: NewsAPIService? = null
        fun getInstance(): NewsAPIService {
            if (apiService == null) {
                apiService = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build().create(NewsAPIService::class.java)
            }
            return apiService!!
        }
    }
}
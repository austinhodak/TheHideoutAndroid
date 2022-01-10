package com.austinhodak.tarkovapi.tarkovtracker

import com.austinhodak.tarkovapi.tarkovtracker.models.TTUser
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.logging.Level


interface TTApiService {

    @GET("progress")
    suspend fun getUserProgress(@Header("Authorization") apiKey: String): Response<TTUser>

    @POST("progress/level/{level}")
    suspend fun setUserLevel(@Header("Authorization") apiKey: String, @Path("level") level: Int): Response<Void>

    @Headers("Content-Type: application/json")
    @POST("progress/quest/{id}")
    suspend fun updateQuest(@Header("Authorization") apiKey: String, @Path("id") id: Int, @Body body: TTUser.TTQuest): Response<Void>

    //@Headers("Content-Type: application/json")
    @POST("progress/quest/objective/{id}")
    suspend fun updateQuestObjective(@Header("Authorization") apiKey: String, @Path("id") id: Int, @Body body: RequestBody): Response<Void>

    @Headers("Content-Type: application/json")
    @POST("progress/hideout/{id}")
    suspend fun updateHideout(@Header("Authorization") apiKey: String, @Path("id") id: Int, @Body body: TTUser.TTQuest): Response<Void>

    @Headers("Content-Type: application/json")
    @POST("progress/hideout/objective/{id}")
    suspend fun updateHideoutObjective(@Header("Authorization") apiKey: String, @Path("id") id: Int, @Body body: TTUser.TTObjective): Response<Void>

    companion object {
        var retrofitService: TTApiService? = null
        fun getInstance() : TTApiService {
            if (retrofitService == null) {
                val logging = HttpLoggingInterceptor()
                logging.setLevel(HttpLoggingInterceptor.Level.BODY)

                val client =  OkHttpClient.Builder()
                    //.addInterceptor(AuthIntercept(UserSettingsModel.ttAPIKey.value))
                    .addInterceptor(logging)
                    .build()

                val retrofit = Retrofit.Builder()
                    .baseUrl("https://tarkovtracker.io/api/v1/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build()
                retrofitService = retrofit.create(TTApiService::class.java)
            }
            return retrofitService!!
        }

    }

}
package com.austinhodak.tarkovapi.tarkovtracker

import com.austinhodak.tarkovapi.tarkovtracker.models.TTUser
import com.austinhodak.tarkovapi.utils.getTTApiKey
import io.github.resilience4j.retrofit.RetryCallAdapter
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import timber.log.Timber
import java.io.IOException
import java.time.Duration
import java.util.concurrent.TimeoutException


interface TTApiService {

    @GET("progress")
    suspend fun getUserProgress(): Response<TTUser>

    @POST("progress/level/{level}")
    suspend fun setUserLevel(@Path("level") level: Int): Response<Void>

    @POST("progress/quest/{id}")
    suspend fun updateQuest(@Path("id") id: Int, @Body body: TTUser.TTQuest): Response<Void>

    @POST("progress/quest/objective/{id}")
    suspend fun updateQuestObjective(@Path("id") id: Int, @Body body: TTUser.TTObjective): Response<Void>

    @POST("progress/hideout/{id}")
    suspend fun updateHideout(@Path("id") id: Int, @Body body: TTUser.TTQuest): Response<Void>

    @POST("progress/hideout/objective/{id}")
    suspend fun updateHideoutObjective(@Path("id") id: Int, @Body body: TTUser.TTObjective): Response<Void>

    companion object {
        var retrofitService: TTApiService? = null
        fun getInstance(): TTApiService {
            if (retrofitService == null) {
                val logging = HttpLoggingInterceptor()
                logging.setLevel(HttpLoggingInterceptor.Level.BODY)

                val retry: Retry = Retry.of("id", RetryConfig.custom<Response<String>>()
                    .maxAttempts(10)
                    .intervalFunction {
                        (1000 * 70).toLong()
                    }
                    .retryOnResult { response: Response<String> ->
                        Timber.d("RETRY RESULT: $response")
                        response.code() == 429
                    }
                    .failAfterMaxAttempts(false)
                    .build())

                val client = OkHttpClient.Builder()
                    .addInterceptor(AuthIntercept(getTTApiKey()))
                    .addInterceptor(logging)
                    .build()

                val retrofit = Retrofit.Builder()
                    //.addCallAdapterFactory()
                    .addCallAdapterFactory(RetryCallAdapter.of(retry))
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
package com.example.weather36h.service

import com.example.weather36h.model.WeatherResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("/api/v1/rest/datastore/F-C0032-001")
    suspend fun get36HourForecast(
        @Query("Authorization") apiKey: String,
        @Query("locationName") locationName: String
    ): WeatherResponse
}

object WeatherApiClient {
    private const val BASE_URL = "https://opendata.cwa.gov.tw/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val service: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(WeatherApiService::class.java)
    }
}
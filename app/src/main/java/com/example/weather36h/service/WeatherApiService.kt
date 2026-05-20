package com.example.weather36h.service

import com.example.weather36h.model.WeatherResponse
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

    val service: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }
}
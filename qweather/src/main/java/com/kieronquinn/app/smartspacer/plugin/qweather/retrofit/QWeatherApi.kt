package com.kieronquinn.app.smartspacer.plugin.qweather.retrofit

import com.kieronquinn.app.smartspacer.plugin.qweather.data.CityLookupResponse
import com.kieronquinn.app.smartspacer.plugin.qweather.data.QWeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface QWeatherApi {
    @GET("/v7/indices/1d")
    suspend fun getIndices(
        @Query("location") location: String,
        @Query("key") key: String,
        @Query("type") type: String
    ): QWeatherResponse

    @GET("/geo/v2/city/lookup")
    suspend fun lookupCity(
        @Query("location") location: String,
        @Query("key") key: String
    ): CityLookupResponse
}
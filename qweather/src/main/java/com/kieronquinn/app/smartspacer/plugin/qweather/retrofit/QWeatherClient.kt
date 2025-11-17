package com.kieronquinn.app.smartspacer.plugin.qweather.retrofit

import com.kieronquinn.app.smartspacer.plugin.qweather.providers.SettingsRepository
import com.kieronquinn.app.smartspacer.plugin.qweather.providers.getBlocking
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class QWeatherClient(private val settings: SettingsRepository) {

    companion object {
        private const val DEFAULT_BASE_URL = "https://devapi.qweather.com/"
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private suspend fun getApi(): QWeatherApi {
        val host = settings.apiHost.first().ifEmpty { DEFAULT_BASE_URL }
        val retrofit = Retrofit.Builder()
            .baseUrl(if (host.endsWith("/")) host else "$host/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
        return retrofit.create(QWeatherApi::class.java)
    }

    suspend fun getIndices(location: String, key: String, type: String) = getApi().getIndices(location, key, type)

    suspend fun lookupCity(location: String, key: String): String? {
        val response = getApi().lookupCity(location, key)
        return response.locations.firstOrNull()?.id
    }
}
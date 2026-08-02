package com.kieronquinn.app.smartspacer.plugin.qweather.retrofit

import com.kieronquinn.app.smartspacer.plugin.qweather.providers.SettingsRepository
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

    private suspend fun getWeatherApi(): QWeatherApi {
        var rawHost = settings.apiHost.first().ifEmpty { DEFAULT_BASE_URL }
        rawHost = rawHost.trim()

        val fixedHost = when {
            rawHost.startsWith("http://") || rawHost.startsWith("https://") -> rawHost
            else -> "https://$rawHost"
        }.let { host ->
            if (host.endsWith("/")) host else "$host/"
        }
        
        return Retrofit.Builder()
            .baseUrl(fixedHost)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
            .create(QWeatherApi::class.java)
    }

    private fun getGeoApi(): QWeatherApi {
        // City lookup (Geo API) always uses the official geoapi endpoints
        return Retrofit.Builder()
            .baseUrl("https://geoapi.qweather.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
            .create(QWeatherApi::class.java)
    }

    suspend fun getIndices(location: String, key: String, type: String) =
        getWeatherApi().getIndices(location, key, type)

    suspend fun lookupCity(location: String, key: String): String? {
        val cleanKey = key.trim()
        val response = getGeoApi().lookupCity(location, cleanKey)
        return response.locations.firstOrNull()?.id
    }
}

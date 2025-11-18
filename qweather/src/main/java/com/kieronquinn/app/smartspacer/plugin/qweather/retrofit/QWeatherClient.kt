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

    private suspend fun getApi(): QWeatherApi {
        val rawHost = settings.apiHost.first().ifEmpty { DEFAULT_BASE_URL }

        // ☆ 统一修正用户输入的 Host（关键）
        val fixedHost = when {
            rawHost.startsWith("http://") || rawHost.startsWith("https://") -> rawHost
            else -> "https://$rawHost"   // 如果用户只输入 domain，则自动补协议
        }.let { host ->
            if (host.endsWith("/")) host else "$host/"
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(fixedHost)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()

        return retrofit.create(QWeatherApi::class.java)
    }

    suspend fun getIndices(location: String, key: String, type: String) =
        getApi().getIndices(location, key, type)

    suspend fun lookupCity(location: String, key: String): String? {
        val response = getApi().lookupCity(location, key)
        return response.locations.firstOrNull()?.id
    }
}

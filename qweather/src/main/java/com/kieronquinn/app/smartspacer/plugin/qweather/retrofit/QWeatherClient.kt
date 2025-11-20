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
        // 1. 获取用户输入的 Host
        var rawHost = settings.apiHost.first().ifEmpty { DEFAULT_BASE_URL }
        
        // 2. 去除首尾空格（防止复制粘贴带空格导致崩溃）
        rawHost = rawHost.trim()

        // 3. 智能修正 Host：自动补 https 和 结尾斜杠
        val fixedHost = when {
            rawHost.startsWith("http://") || rawHost.startsWith("https://") -> rawHost
            else -> "https://$rawHost"
        }.let { host ->
            if (host.endsWith("/")) host else "$host/"
        }

        // 此时 fixedHost 应该是 "https://qc5vxmpxbq.re.qweatherapi.com/"
        
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
        // 这里的 key 也建议 trim() 一下，防止用户输入时后面带空格
        val cleanKey = key.trim()
        val response = getApi().lookupCity(location, cleanKey)
        // 取第一个城市的 ID
        return response.locations.firstOrNull()?.id
    }
}

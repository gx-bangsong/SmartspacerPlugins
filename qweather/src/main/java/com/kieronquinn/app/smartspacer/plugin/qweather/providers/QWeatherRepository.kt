package com.kieronquinn.app.smartspacer.plugin.qweather.providers

import com.kieronquinn.app.smartspacer.plugin.qweather.data.QWeatherResponse
import com.kieronquinn.app.smartspacer.plugin.qweather.retrofit.QWeatherClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.util.Log
import kotlinx.coroutines.flow.first

interface QWeatherRepository {
    val weatherData: StateFlow<QWeatherResponse?>
    val previousWeatherData: StateFlow<QWeatherResponse?>
    suspend fun setWeatherData(data: QWeatherResponse)
    suspend fun fetchWeatherData(): QWeatherResponse?
}

class QWeatherRepositoryImpl(
    private val client: QWeatherClient,
    private val settings: SettingsRepository
) : QWeatherRepository {
    private val _weatherData = MutableStateFlow<QWeatherResponse?>(null)
    override val weatherData: StateFlow<QWeatherResponse?> = _weatherData

    private val _previousWeatherData = MutableStateFlow<QWeatherResponse?>(null)
    override val previousWeatherData: StateFlow<QWeatherResponse?> = _previousWeatherData

    override suspend fun setWeatherData(data: QWeatherResponse) {
        // Shift current data to previous
        _previousWeatherData.value = _weatherData.value
        // Set new data
        _weatherData.value = data
    }

    override suspend fun fetchWeatherData(): QWeatherResponse? {
        val apiKey = settings.apiKey.first()
        val locationName = settings.locationName.first()
        val selectedIndices = settings.selectedIndices.first()

        if (apiKey.isEmpty()) {
            Log.d("QWeatherRepository", "API key is empty")
            return null
        }
        if (locationName.isEmpty()) {
            Log.d("QWeatherRepository", "Location name is empty")
            return null
        }

        Log.d("QWeatherRepository", "Fetching weather data for $locationName with key $apiKey")

        val locationId = settings.locationId ?: client.lookupCity(locationName, apiKey)
        if (locationId == null) {
            Log.d("QWeatherRepository", "Failed to lookup city ID for $locationName")
            settings.setCityLookupFailed(true)
            return null
        }

        settings.locationId = locationId
        settings.setCityLookupFailed(false)

        return client.getIndices(locationId, apiKey, selectedIndices)
    }
}
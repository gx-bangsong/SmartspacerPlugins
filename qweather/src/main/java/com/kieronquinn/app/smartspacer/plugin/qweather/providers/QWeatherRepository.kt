package com.kieronquinn.app.smartspacer.plugin.qweather.providers

import com.kieronquinn.app.smartspacer.plugin.qweather.data.QWeatherResponse
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow

interface QWeatherRepository {
    val weatherData: StateFlow<QWeatherResponse?>
    val previousWeatherData: StateFlow<QWeatherResponse?>
    suspend fun setWeatherData(data: QWeatherResponse)
}

class QWeatherRepositoryImpl : QWeatherRepository {
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
}
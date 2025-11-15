package com.kieronquinn.app.smartspacer.plugin.qweather.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.kieronquinn.app.smartspacer.plugin.qweather.complications.QWeatherComplication
import com.kieronquinn.app.smartspacer.plugin.qweather.providers.QWeatherRepository
import com.kieronquinn.app.smartspacer.plugin.qweather.providers.SettingsRepository
import com.kieronquinn.app.smartspacer.plugin.qweather.providers.getBlocking
import com.kieronquinn.app.smartspacer.plugin.qweather.retrofit.QWeatherClient
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class UpdateReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        private const val TAG = "QWeatherUpdateReceiver"
        const val EXTRA_SMARTSPACER_ID = "smartspacerId"
    }

    private val settingsRepository by inject<SettingsRepository>()
    private val qWeatherRepository by inject<QWeatherRepository>()

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val smartspacerId = intent.getStringExtra(EXTRA_SMARTSPACER_ID)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiKey = settingsRepository.apiKey.getBlocking()
                val locationName = settingsRepository.locationName.getBlocking()
                val selectedIndices = settingsRepository.selectedIndices.getBlocking()

                if (apiKey.isBlank() || locationName.isBlank()) {
                    Log.d(TAG, "API key or location not set, skipping update.")
                    return@launch
                }

                val locationId = QWeatherClient.lookupCity(locationName, apiKey)
                if (locationId == null) {
                    Log.e(TAG, "Failed to lookup city: $locationName")
                    settingsRepository.setCityLookupFailed(true)
                    return@launch
                }

                settingsRepository.setCityLookupFailed(false)
                val response = QWeatherClient.getIndices(locationId, apiKey, selectedIndices)
                qWeatherRepository.setWeatherData(response)
                Log.d(TAG, "Successfully fetched and saved weather data.")

                // 使用提供者类的引用来调用 notifyChange
                SmartspacerComplicationProvider.notifyChange(context, QWeatherComplication::class.java, smartspacerId)

            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch weather data", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}

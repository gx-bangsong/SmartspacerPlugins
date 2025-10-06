package com.kieronquinn.app.smartspacer.plugin.qweather.receivers

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import com.kieronquinn.app.smartspacer.plugin.qweather.complications.QWeatherComplication
import com.kieronquinn.app.smartspacer.plugin.qweather.providers.QWeatherRepository
import com.kieronquinn.app.smartspacer.plugin.qweather.providers.SettingsRepository
import com.kieronquinn.app.smartspacer.plugin.qweather.retrofit.QWeatherClient
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import kotlinx.coroutines.GlobalScope
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
        val smartspacerId = intent.getStringExtra(EXTRA_SMARTSPACER_ID)
        if (smartspacerId == null) {
            Log.e(TAG, "smartspacerId is null, cannot update complication")
            return
        }

        GlobalScope.launch {
            val apiKey = settingsRepository.apiKey.get()
            val locationId = settingsRepository.locationId.get()
            val selectedIndices = settingsRepository.selectedIndices.get()

            if (apiKey.isBlank() || locationId.isBlank()) {
                Log.d(TAG, "API key or location not set, skipping update.")
                return@launch
            }

            try {
                val response = QWeatherClient.instance.getIndices(locationId, apiKey, selectedIndices)
                qWeatherRepository.setWeatherData(response)
                Log.d(TAG, "Successfully fetched and saved weather data.")
                val componentName = ComponentName(context, QWeatherComplication::class.java)
                SmartspacerComplicationProvider.notifyChange(context, componentName, smartspacerId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch weather data", e)
            }
        }
    }
}
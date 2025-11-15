package com.kieronquinn.app.smartspacer.plugin.qweather.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.kieronquinn.app.smartspacer.plugin.qweather.complications.QWeatherComplication
import com.kieronquinn.app.smartspacer.plugin.qweather.providers.QWeatherRepository
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

    private val qWeatherRepository by inject<QWeatherRepository>()

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val smartspacerId = intent.getStringExtra(EXTRA_SMARTSPACER_ID)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val weatherData = qWeatherRepository.fetchWeatherData()
                if (weatherData != null) {
                    qWeatherRepository.setWeatherData(weatherData)
                    Log.d(TAG, "Successfully fetched and saved weather data.")
                } else {
                    Log.d(TAG, "Failed to fetch weather data, skipping update.")
                }

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

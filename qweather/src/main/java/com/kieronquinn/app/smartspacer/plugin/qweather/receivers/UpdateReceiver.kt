package com.kieronquinn.app.smartspacer.plugin.qweather.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.kieronquinn.app.smartspacer.plugin.qweather.complications.QWeatherComplication
import android.app.AlarmManager
import android.app.PendingIntent
import com.kieronquinn.app.smartspacer.plugin.qweather.providers.QWeatherRepository
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class UpdateReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        private const val TAG = "QWeatherUpdateReceiver"
        const val EXTRA_SMARTSPACER_ID = "smartspacerId"
        const val EXTRA_LOCATION_NAME = "extra_location_name"
        const val EXTRA_API_KEY = "extra_api_key"
        const val EXTRA_SELECTED_INDICES = "extra_selected_indices"
    }

    private val qWeatherRepository by inject<QWeatherRepository>()
    private val settingsRepository by inject<SettingsRepository>()
    private val qWeatherClient by inject<QWeatherClient>()

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val smartspacerId = intent.getStringExtra(EXTRA_SMARTSPACER_ID)
        val apiKey = intent.getStringExtra(EXTRA_API_KEY)
        val locationName = intent.getStringExtra(EXTRA_LOCATION_NAME)
        val selectedIndices = intent.getStringExtra(EXTRA_SELECTED_INDICES)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val weatherData = qWeatherRepository.fetchWeatherData()
                if (weatherData != null) {
                    qWeatherRepository.setWeatherData(weatherData)
                    Log.d(TAG, "Successfully fetched and saved weather data.")
                    scheduleNextUpdate(context, smartspacerId)
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

    private fun scheduleNextUpdate(context: Context, smartspacerId: String?) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, UpdateReceiver::class.java).apply {
            putExtra(EXTRA_SMARTSPACER_ID, smartspacerId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1),
            pendingIntent
        )
    }
}

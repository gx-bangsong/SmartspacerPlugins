package com.kieronquinn.app.smartspacer.plugin.qweather.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.kieronquinn.app.smartspacer.plugin.qweather.complications.QWeatherComplication
import android.app.AlarmManager
import android.app.PendingIntent
import com.kieronquinn.app.smartspacer.plugin.qweather.providers.QWeatherRepository
import com.kieronquinn.app.smartspacer.plugin.qweather.providers.SettingsRepository
import com.kieronquinn.app.smartspacer.plugin.qweather.retrofit.QWeatherClient
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class UpdateReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        private const val TAG = "QWeatherUpdateReceiver"
        const val EXTRA_SMARTSPACER_ID = "smartspacerId"
    }

    private val qWeatherRepository by inject<QWeatherRepository>()
    private val settingsRepository by inject<SettingsRepository>()
    private val qWeatherClient by inject<QWeatherClient>()

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val smartspacerId = intent.getStringExtra(EXTRA_SMARTSPACER_ID)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Delay to allow preferences to save
                kotlinx.coroutines.delay(500)
                val apiKey = settingsRepository.apiKey.first()
                val locationName = settingsRepository.locationName.first()
                val selectedIndices = settingsRepository.selectedIndices.first()
                if(apiKey.isEmpty() || locationName.isEmpty()){
                    Log.d(TAG, "API key or location name is empty, skipping update.")
                    return@launch
                }
                Log.d(TAG, "Looking up city: $locationName")
                val locationId = qWeatherClient.lookupCity(locationName, apiKey)
                if(locationId != null){
                    Log.d(TAG, "Found location ID: $locationId, fetching indices.")
                    val weatherData = qWeatherClient.getIndices(locationId, apiKey, selectedIndices)
                    qWeatherRepository.setWeatherData(weatherData)
                    Log.d(TAG, "Successfully fetched and saved weather data.")
                    scheduleNextUpdate(context, smartspacerId)
                }else{
                    settingsRepository.setCityLookupFailed(true)
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

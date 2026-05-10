package com.kieronquinn.app.smartspacer.plugin.qweather.work

import android.content.Context
import android.util.Log
import androidx.work.*
import com.kieronquinn.app.smartspacer.plugin.qweather.complications.QWeatherComplication
import com.kieronquinn.app.smartspacer.plugin.qweather.providers.QWeatherRepository
import com.kieronquinn.app.smartspacer.plugin.qweather.providers.SettingsRepository
import com.kieronquinn.app.smartspacer.plugin.qweather.retrofit.QWeatherClient
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

class QWeatherWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    companion object {
        private const val TAG = "QWeatherWorker"
        private const val WORK_NAME_PERIODIC = "qweather_update_periodic"
        private const val WORK_NAME_ONE_TIME = "qweather_update_one_time"

        fun enqueuePeriodic(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<QWeatherWorker>(1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME_PERIODIC,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun enqueueImmediate(context: Context) {
            val request = OneTimeWorkRequestBuilder<QWeatherWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME_ONE_TIME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }

    private val qWeatherRepository by inject<QWeatherRepository>()
    private val settingsRepository by inject<SettingsRepository>()
    private val qWeatherClient by inject<QWeatherClient>()

    override suspend fun doWork(): Result {
        Log.d(TAG, "Worker started")

        return withTimeoutOrNull(15000) { // 15 seconds total timeout
            try {
                val apiKey = settingsRepository.apiKey.first()
                val locationName = settingsRepository.locationName.first()
                val selectedIndices = settingsRepository.selectedIndices.first()

                if (apiKey.isEmpty() || locationName.isEmpty()) {
                    Log.d(TAG, "API key or location name is empty, skipping update.")
                    return@withTimeoutOrNull Result.success()
                }

                Log.d(TAG, "Looking up city: $locationName")
                val locationId = qWeatherClient.lookupCity(locationName, apiKey)

                if (locationId != null) {
                    Log.d(TAG, "Found location ID: $locationId, fetching indices.")
                    val weatherData = qWeatherClient.getIndices(locationId, apiKey, selectedIndices)
                    qWeatherRepository.setWeatherData(weatherData)
                    Log.d(TAG, "Successfully fetched and saved weather data.")
                    settingsRepository.setCityLookupFailed(false)
                } else {
                    settingsRepository.setCityLookupFailed(true)
                    Log.d(TAG, "Failed to find location, skipping update.")
                }

                SmartspacerComplicationProvider.notifyChange(context, QWeatherComplication::class.java)
                Result.success()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch weather data", e)
                Result.retry()
            }
        } ?: run {
            Log.e(TAG, "Worker timed out")
            Result.retry()
        }
    }
}

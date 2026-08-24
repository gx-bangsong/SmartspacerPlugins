package com.kieronquinn.app.smartspacer.plugin.qweather.work

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.kieronquinn.app.smartspacer.plugin.qweather.complications.QWeatherComplication
import com.kieronquinn.app.smartspacer.plugin.qweather.providers.SettingsRepository
import com.kieronquinn.app.smartspacer.plugin.qweather.providers.getBlocking
import com.kieronquinn.app.smartspacer.plugin.qweather.utils.LifestyleDisplayWindow
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

/**
 * Refreshes the complication at the next bedtime / wake show-or-hide boundary.
 */
class QWeatherVisibilityWorker(
    private val context: Context,
    params: WorkerParameters
) : Worker(context, params), KoinComponent {

    companion object {
        private const val WORK_NAME = "qweather_visibility_boundary"

        fun scheduleNext(context: Context, settings: SettingsRepository) {
            val delay = LifestyleDisplayWindow.millisUntilNextTransition(
                nowEpochMillis = System.currentTimeMillis(),
                bedtimeEnabled = settings.bedtimeEnabled.getBlocking(),
                bedtimeStartMinutes = settings.bedtimeMinutes.getBlocking(),
                wakeEnabled = settings.wakeEnabled.getBlocking(),
                wakeStartMinutes = settings.wakeMinutes.getBlocking(),
                durationMinutes = settings.displayDurationMinutes.getBlocking()
            )
            val workManager = WorkManager.getInstance(context)
            if (delay == null) {
                workManager.cancelUniqueWork(WORK_NAME)
                return
            }
            val request = OneTimeWorkRequestBuilder<QWeatherVisibilityWorker>()
                .setInitialDelay(delay.coerceAtLeast(1_000L), TimeUnit.MILLISECONDS)
                .build()
            workManager.enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
        }
    }

    private val settingsRepository by inject<SettingsRepository>()

    override fun doWork(): Result {
        SmartspacerComplicationProvider.notifyChange(context, QWeatherComplication::class.java)
        scheduleNext(context, settingsRepository)
        return Result.success()
    }
}

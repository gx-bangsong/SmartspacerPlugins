package com.kieronquinn.app.smartspacer.plugin.waterreminder.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kieronquinn.app.smartspacer.plugin.waterreminder.data.WaterReminderSettings
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WaterReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val settings: WaterReminderSettings by inject()

    override suspend fun doWork(): Result {
        if (settings.remindersEnabled) {
            scheduleReminders()
        }
        scheduleDailyReset()
        return Result.success()
    }

    private fun scheduleReminders() {
        val now = java.util.Calendar.getInstance()
        val nowInMillis = now.timeInMillis
        val startHour = settings.activeHoursStart / 60
        val startMinute = settings.activeHoursStart % 60
        val endHour = settings.activeHoursEnd / 60
        val endMinute = settings.activeHoursEnd % 60

        val startCalendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, startHour)
            set(java.util.Calendar.MINUTE, startMinute)
        }
        val endCalendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, endHour)
            set(java.util.Calendar.MINUTE, endMinute)
        }

        if (nowInMillis in startCalendar.timeInMillis..endCalendar.timeInMillis) {
            val totalCups = settings.dailyGoal / settings.cupSize
            val activeDuration = (settings.activeHoursEnd - settings.activeHoursStart).toLong()
            val interval = activeDuration / totalCups

            var nextReminderTime = startCalendar.timeInMillis
            while (nextReminderTime < endCalendar.timeInMillis) {
                if (nextReminderTime > nowInMillis) {
                    val delay = nextReminderTime - nowInMillis
                    val workRequest = androidx.work.OneTimeWorkRequestBuilder<NotificationWorker>()
                        .setInitialDelay(delay, java.util.concurrent.TimeUnit.MINUTES)
                        .build()
                    androidx.work.WorkManager.getInstance(applicationContext).enqueue(workRequest)
                }
                nextReminderTime += interval * 60 * 1000
            }
        }
    }

    private fun scheduleDailyReset() {
        val midnight = java.util.Calendar.getInstance().apply {
            add(java.util.Calendar.DAY_OF_YEAR, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
        }
        val delay = midnight.timeInMillis - System.currentTimeMillis()
        val workRequest = androidx.work.OneTimeWorkRequestBuilder<ResetWorker>()
            .setInitialDelay(delay, java.util.concurrent.TimeUnit.MILLISECONDS)
            .build()
        androidx.work.WorkManager.getInstance(applicationContext).enqueue(workRequest)
    }
}
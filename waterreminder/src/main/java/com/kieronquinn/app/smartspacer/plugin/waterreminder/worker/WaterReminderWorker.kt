package com.kieronquinn.app.smartspacer.plugin.waterreminder.worker

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.kieronquinn.app.smartspacer.plugin.waterreminder.data.WaterReminderSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.waterreminder.providers.WaterReminderProvider
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class WaterReminderWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    companion object {
        const val WORKER_TAG = "water_reminder_worker"
        const val NOTIFICATION_WORKER_TAG = "water_notification_worker"
    }

    private val settingsRepository: WaterReminderSettingsRepository by inject()
    private val workManager = WorkManager.getInstance(appContext)

    override suspend fun doWork(): Result {
        Log.d(WORKER_TAG, "Water reminder worker running...")
        checkForDailyReset()
        scheduleNextNotification()
        notifyProvider()
        return Result.success()
    }

    private suspend fun checkForDailyReset() {
        val lastResetMillis = settingsRepository.lastResetTimestamp.get()
        val lastResetDate = Instant.ofEpochMilli(lastResetMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        val today = LocalDate.now()

        if (lastResetDate.isBefore(today)) {
            Log.d(WORKER_TAG, "New day detected. Resetting water intake progress.")
            settingsRepository.currentProgressCups.set(0)
            settingsRepository.lastResetTimestamp.set(System.currentTimeMillis())
            workManager.cancelAllWorkByTag(NOTIFICATION_WORKER_TAG) // Cancel any leftover notifications
        }
    }

    private suspend fun scheduleNextNotification() {
        val goalMl = settingsRepository.dailyGoalMl.get()
        val cupSizeMl = settingsRepository.cupSizeMl.get()
        if (goalMl <= 0 || cupSizeMl <= 0) return

        val goalCups = (goalMl + cupSizeMl - 1) / cupSizeMl
        val progressCups = settingsRepository.currentProgressCups.get()
        if (progressCups >= goalCups) {
            Log.d(WORKER_TAG, "Goal met, no more notifications today.")
            workManager.cancelAllWorkByTag(NOTIFICATION_WORKER_TAG) // Cancel any pending notifications
            return
        }

        val nextReminderTime = calculateNextReminderTime()
        if (nextReminderTime != null) {
            val now = LocalTime.now()
            val delay = Duration.between(now, nextReminderTime)
            if (!delay.isNegative) {
                val workRequest = OneTimeWorkRequestBuilder<WaterNotificationWorker>()
                    .setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
                    .addTag(NOTIFICATION_WORKER_TAG)
                    .build()
                workManager.enqueue(workRequest)
                Log.d(WORKER_TAG, "Scheduled next notification for $nextReminderTime (in $delay)")
            }
        }
    }

    private suspend fun calculateNextReminderTime(): LocalTime? {
        val startHour = settingsRepository.activeHourStart.get()
        val endHour = settingsRepository.activeHourEnd.get()
        val goalCups = (settingsRepository.dailyGoalMl.get() + settingsRepository.cupSizeMl.get() - 1) / settingsRepository.cupSizeMl.get()
        val progressCups = settingsRepository.currentProgressCups.get()
        val now = LocalTime.now()

        if (progressCups >= goalCups || now.hour > endHour) return null

        val remainingCups = goalCups - progressCups
        if (remainingCups <= 0) return null

        val effectiveStartHour = if (now.hour < startHour) startHour else now.hour
        val remainingHours = (endHour - effectiveStartHour).coerceAtLeast(1)
        val intervalMinutes = (remainingHours * 60) / remainingCups

        return now.plusMinutes(intervalMinutes.toLong())
    }

    private fun notifyProvider() {
        val intent = Intent(appContext, WaterReminderProvider::class.java).apply {
            action = WaterReminderProvider.ACTION_REFRESH
        }
        appContext.sendBroadcast(intent)
        Log.d(WORKER_TAG, "Sent refresh broadcast to provider.")
    }
}
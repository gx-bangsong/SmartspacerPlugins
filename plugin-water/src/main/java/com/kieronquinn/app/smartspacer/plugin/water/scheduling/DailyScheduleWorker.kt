package com.kieronquinn.app.smartspacer.plugin.water.scheduling

import android.content.Context
import androidx.work.*
import com.kieronquinn.app.smartspacer.plugin.water.repositories.DailySchedule
import com.kieronquinn.app.smartspacer.plugin.water.repositories.WaterDataRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import kotlin.math.ceil

class DailyScheduleWorker(
    appContext: Context,
    workerParams: WorkerParameters
): CoroutineWorker(appContext, workerParams), KoinComponent {

    private val waterDataRepository by inject<WaterDataRepository>()
    private val waterScheduler by inject<WaterScheduler>()

    override suspend fun doWork(): Result {
        val today = LocalDate.now()
        val totalCups = ceil(waterDataRepository.dailyGoalMl.toDouble() / waterDataRepository.cupMl).toInt()

        val scheduleTimes = waterScheduler.computeDailySchedule(
            date = today,
            startMinutes = waterDataRepository.activeStartMinutes,
            endMinutes = waterDataRepository.activeEndMinutes,
            totalCups = totalCups,
            centerInWindow = true // You could make this a setting
        )

        val dailySchedule = DailySchedule(
            date = today.toString(),
            scheduledTimes = scheduleTimes,
            fulfilledMask = List(totalCups) { false },
            cupsTotal = totalCups,
            fulfilledCount = 0
        )

        waterDataRepository.setDailySchedule(today, dailySchedule)

        // Schedule the alarms for the computed times
        waterScheduler.scheduleAlarmsForDate(applicationContext, scheduleTimes)

        // Schedule the next run for tomorrow
        scheduleNextRun(applicationContext)

        return Result.success()
    }

    private fun scheduleNextRun(context: Context) {
        val workManager = WorkManager.getInstance(context)
        val request = OneTimeWorkRequestBuilder<DailyScheduleWorker>()
            .setInitialDelay(1, TimeUnit.DAYS)
            .build()
        workManager.enqueueUniqueWork("DailyScheduleWorker", ExistingWorkPolicy.REPLACE, request)
    }
}
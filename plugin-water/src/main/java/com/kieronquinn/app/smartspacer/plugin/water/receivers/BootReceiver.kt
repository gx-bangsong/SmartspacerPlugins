package com.kieronquinn.app.smartspacer.plugin.water.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.water.repositories.WaterDataRepository
import com.kieronquinn.app.smartspacer.plugin.water.scheduling.WaterScheduler
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDate
import kotlin.math.ceil

class BootReceiver : BroadcastReceiver(), KoinComponent {

    private val waterScheduler by inject<WaterScheduler>()
    private val waterDataRepository by inject<WaterDataRepository>()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val today = LocalDate.now()
            val totalCups = ceil(waterDataRepository.dailyGoalMl.toDouble() / waterDataRepository.cupMl).toInt()
            val schedule = waterScheduler.computeDailySchedule(
                today,
                waterDataRepository.activeStartMinutes,
                waterDataRepository.activeEndMinutes,
                totalCups
            )
            waterScheduler.scheduleAlarmsForDate(context, schedule)
        }
    }
}

package com.kieronquinn.app.smartspacer.plugin.water.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.kieronquinn.app.smartspacer.plugin.water.repositories.WaterDataRepository
import com.kieronquinn.app.smartspacer.plugin.water.scheduling.WaterScheduler
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDate
import java.time.ZoneId

class WaterActionReceiver : BroadcastReceiver(), KoinComponent {

    private val waterDataRepository by inject<WaterDataRepository>()
    private val waterScheduler by inject<WaterScheduler>()

    companion object {
        const val TAG = "WaterActionReceiver"
        const val ACTION_MARK_AS_DRUNK = "com.kieronquinn.app.smartspacer.plugin.water.ACTION_MARK_AS_DRUNK"
        const val ACTION_SNOOZE = "com.kieronquinn.app.smartspacer.plugin.water.ACTION_SNOOZE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_MARK_AS_DRUNK -> {
                Log.d(TAG, "Mark as Drunk action received")
                markCupsFulfilled(context, 1)
            }
            ACTION_SNOOZE -> {
                Log.d(TAG, "Snooze action received")
                val reminderTime = intent.getLongExtra(WaterReminderReceiver.EXTRA_REMINDER_TIME, 0L)
                if (reminderTime > 0) {
                    snoozeReminder(context, reminderTime)
                }
            }
        }
    }

    private fun snoozeReminder(context: Context, reminderTime: Long) {
        val snoozeMinutes = waterDataRepository.snoozeMinutes
        val snoozeTime = System.currentTimeMillis() + snoozeMinutes * 60 * 1000

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        val intent = Intent(context, WaterReminderReceiver::class.java).apply {
            putExtra(WaterReminderReceiver.EXTRA_REMINDER_TIME, reminderTime)
        }
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            reminderTime.toInt(),
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, snoozeTime, pendingIntent)
    }

    private fun markCupsFulfilled(context: Context, count: Int) {
        val today = LocalDate.now()
        val schedule = waterDataRepository.getDailySchedule(today) ?: return

        var markedCount = 0
        val newMask = schedule.fulfilledMask.toMutableList()
        for (i in newMask.indices) {
            if (!newMask[i] && markedCount < count) {
                newMask[i] = true
                markedCount++
            }
        }
        schedule.fulfilledCount += markedCount

        val newSchedule = schedule.copy(fulfilledMask = newMask)
        waterDataRepository.setDailySchedule(today, newSchedule)

        if (waterDataRepository.smartAdjust) {
            // Cancel old alarms
            waterScheduler.cancelAlarmsForDate(context, schedule.scheduledTimes)

            // Recalculate and reschedule remaining alarms
            val remainingCups = schedule.cupsTotal - schedule.fulfilledCount
            if (remainingCups > 0) {
                val nowMinutes = java.time.LocalTime.now().toSecondOfDay() / 60
                val endMinutes = waterDataRepository.activeEndMinutes
                val newScheduledTimes = waterScheduler.computeDailySchedule(
                    date = today,
                    startMinutes = nowMinutes,
                    endMinutes = endMinutes,
                    totalCups = remainingCups,
                    centerInWindow = true
                )
                waterScheduler.scheduleAlarmsForDate(context, newScheduledTimes)
            }
        }
    }
}
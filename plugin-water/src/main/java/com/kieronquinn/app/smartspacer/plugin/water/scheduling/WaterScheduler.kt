package com.kieronquinn.app.smartspacer.plugin.water.scheduling

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.water.receivers.WaterReminderReceiver
import java.time.LocalDate
import java.time.ZoneId

class WaterScheduler {

    /**
     * Returns a list of epochMillis timestamps for a given day's reminders.
     * @param date The date for which to compute the schedule.
     * @param startMinutes The start of the active window, in minutes since midnight.
     * @param endMinutes The end of the active window, in minutes since midnight.
     * @param totalCups The total number of cups to be consumed.
     * @param centerInWindow If true, the reminders are centered within their intervals.
     * @return A list of timestamps in epoch milliseconds.
     */
    fun computeDailySchedule(
        date: LocalDate,
        startMinutes: Int,
        endMinutes: Int,
        totalCups: Int,
        centerInWindow: Boolean = true
    ): List<Long> {
        if (totalCups <= 0) {
            return emptyList()
        }

        val startTs = date.atStartOfDay(ZoneId.systemDefault()).plusMinutes(startMinutes.toLong()).toInstant().toEpochMilli()
        val endTs = date.atStartOfDay(ZoneId.systemDefault()).plusMinutes(endMinutes.toLong()).toInstant().toEpochMilli()

        val activeSeconds = (endTs - startTs) / 1000.0
        if (activeSeconds <= 0) {
            return emptyList()
        }

        val intervalSeconds = activeSeconds / totalCups
        val reminders = mutableListOf<Long>()

        for (i in 0 until totalCups) {
            val offset = if (centerInWindow) {
                (i + 0.5) * intervalSeconds
            } else {
                i.toDouble() * intervalSeconds
            }
            val reminderTime = startTs + (offset * 1000).toLong()
            reminders.add(reminderTime)
        }
        return reminders
    }

    fun scheduleAlarmsForDate(context: Context, scheduledTimes: List<Long>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        scheduledTimes.forEachIndexed { index, time ->
            val intent = Intent(context, WaterReminderReceiver::class.java).apply {
                putExtra(WaterReminderReceiver.EXTRA_REMINDER_TIME, time)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                index, // Using index as request code for uniqueness
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
        }
    }

    fun cancelAlarmsForDate(context: Context, scheduledTimes: List<Long>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        scheduledTimes.forEachIndexed { index, time ->
            val intent = Intent(context, WaterReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                index,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }
}
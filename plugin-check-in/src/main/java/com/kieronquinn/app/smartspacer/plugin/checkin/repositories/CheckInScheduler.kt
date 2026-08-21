package com.kieronquinn.app.smartspacer.plugin.checkin.repositories

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import com.kieronquinn.app.smartspacer.plugin.checkin.receivers.CheckInAlarmReceiver
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.PendingIntent_MUTABLE_FLAGS
import kotlinx.coroutines.flow.first
import java.util.Calendar

interface CheckInScheduler {
    fun hasPermission(): Boolean
    suspend fun scheduleDailyAlarms()
    fun cancelAlarms()
}

class CheckInSchedulerImpl(
    private val context: Context,
    private val settingsRepository: CheckInSettingsRepository
) : CheckInScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun hasPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return alarmManager.canScheduleExactAlarms()
    }

    override suspend fun scheduleDailyAlarms() {
        cancelAlarms()

        val isEnabled = settingsRepository.isReminderEnabled.first()
        if (!isEnabled || !hasPermission()) {
            return
        }

        val checkInOnly = settingsRepository.checkInOnly.first()
        val startTime = settingsRepository.workStartTime.first()
        val endTime = settingsRepository.workEndTime.first()

        scheduleSpecificAlarm(startTime, 1, CheckInAlarmReceiver.TYPE_START)
        if (!checkInOnly) {
            scheduleSpecificAlarm(endTime, 2, CheckInAlarmReceiver.TYPE_END)
        }
    }

    private fun scheduleSpecificAlarm(timeStr: String, reqCode: Int, type: String) {
        val parts = timeStr.split(":")
        if (parts.size < 2) return
        val hour = parts[0].toIntOrNull() ?: 8
        val minute = parts[1].toIntOrNull() ?: 30

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val now = System.currentTimeMillis()
        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val intent = CheckInAlarmReceiver.createIntent(context, type)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reqCode,
            intent,
            PendingIntent_MUTABLE_FLAGS
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    override fun cancelAlarms() {
        cancelSpecificAlarm(1, CheckInAlarmReceiver.TYPE_START)
        cancelSpecificAlarm(2, CheckInAlarmReceiver.TYPE_END)
    }

    private fun cancelSpecificAlarm(reqCode: Int, type: String) {
        val intent = CheckInAlarmReceiver.createIntent(context, type)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reqCode,
            intent,
            PendingIntent_MUTABLE_FLAGS or PendingIntent.FLAG_NO_CREATE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}

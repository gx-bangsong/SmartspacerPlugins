package com.kieronquinn.app.smartspacer.plugin.travel.repositories

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import com.kieronquinn.app.smartspacer.plugin.travel.data.TravelInfoItem
import com.kieronquinn.app.smartspacer.plugin.travel.data.TravelInfoDao
import com.kieronquinn.app.smartspacer.plugin.travel.notifications.TravelNotificationController
import com.kieronquinn.app.smartspacer.plugin.travel.receivers.TravelAlarmReceiver
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.PendingIntent_MUTABLE_FLAGS

interface TravelScheduler {
    fun hasPermission(): Boolean
    fun scheduleReminder(item: TravelInfoItem)
    fun cancelReminder(itemId: Int)
    suspend fun rescheduleAll()
}

/**
 * Owns the two alarms per trip:
 *  - the T-30 "departure window" alarm (existing behaviour, kept as the single source of truth
 *    for the reminder window);
 *  - a cleanup alarm at departure + grace period that cancels the Live Update so no permanent
 *    ongoing notification can outlive the trip.
 */
class TravelSchedulerImpl(
    private val context: Context,
    private val travelInfoDao: TravelInfoDao
) : TravelScheduler {

    companion object {
        const val DEPARTURE_WINDOW_MS = TravelNotificationController.DEPARTURE_WINDOW_MS
        const val GRACE_PERIOD_MS = TravelNotificationController.GRACE_PERIOD_MS

        // Distinct request codes for the two alarms of the same trip.
        private const val REQUEST_REMINDER_OFFSET = 0
        private const val REQUEST_CLEANUP_OFFSET = 1_000_000
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun hasPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return alarmManager.canScheduleExactAlarms()
    }

    override fun scheduleReminder(item: TravelInfoItem) {
        cancelReminder(item.id)

        if (!hasPermission() || item.isUsed) {
            return
        }

        val now = System.currentTimeMillis()

        // 提前 30 分钟进入出发窗口
        val reminderTime = item.departureTime - DEPARTURE_WINDOW_MS
        if (reminderTime > now) {
            scheduleAlarm(item.id, reminderTime, REQUEST_REMINDER_OFFSET, TravelAlarmReceiver.ACTION_REMINDER)
        }

        // 出发 + 宽限期后取消 Live Update，避免留下永久 ongoing 通知
        val cleanupTime = item.departureTime + GRACE_PERIOD_MS
        if (cleanupTime > now) {
            scheduleAlarm(item.id, cleanupTime, REQUEST_CLEANUP_OFFSET, TravelAlarmReceiver.ACTION_CLEANUP)
        }
    }

    private fun scheduleAlarm(itemId: Int, triggerAtMillis: Long, requestCodeOffset: Int, action: String) {
        val intent = TravelAlarmReceiver.createIntent(context, itemId, action)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            itemId + requestCodeOffset,
            intent,
            PendingIntent_MUTABLE_FLAGS
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    override fun cancelReminder(itemId: Int) {
        cancelAlarm(itemId, REQUEST_REMINDER_OFFSET, TravelAlarmReceiver.ACTION_REMINDER)
        cancelAlarm(itemId, REQUEST_CLEANUP_OFFSET, TravelAlarmReceiver.ACTION_CLEANUP)
    }

    private fun cancelAlarm(itemId: Int, requestCodeOffset: Int, action: String) {
        val intent = TravelAlarmReceiver.createIntent(context, itemId, action)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            itemId + requestCodeOffset,
            intent,
            PendingIntent_MUTABLE_FLAGS or PendingIntent.FLAG_NO_CREATE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    override suspend fun rescheduleAll() {
        val now = System.currentTimeMillis()
        val trips = travelInfoDao.getUnusedTrips(now)
        for (trip in trips) {
            scheduleReminder(trip)
        }
    }
}

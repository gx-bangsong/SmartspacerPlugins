package com.kieronquinn.app.smartspacer.plugin.travel.repositories

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import com.kieronquinn.app.smartspacer.plugin.shared.permissions.ExactAlarmCompat
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.PendingIntent_MUTABLE_FLAGS
import com.kieronquinn.app.smartspacer.plugin.travel.data.TravelInfoDao
import com.kieronquinn.app.smartspacer.plugin.travel.data.TravelInfoItem
import com.kieronquinn.app.smartspacer.plugin.travel.notifications.TravelLiveUpdateGate
import com.kieronquinn.app.smartspacer.plugin.travel.notifications.TravelNotificationController
import com.kieronquinn.app.smartspacer.plugin.travel.receivers.TravelAlarmReceiver

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
 *
 * Exact-alarm permission is a special setting, not a runtime permission. When it is missing the
 * scheduler uses [AlarmManager.setAndAllowWhileIdle] instead of silently dropping the reminder.
 */
class TravelSchedulerImpl(
    private val context: Context,
    private val travelInfoDao: TravelInfoDao,
    private val notificationController: TravelNotificationController,
    private val suppressionRepository: TravelSuppressionRepository
) : TravelScheduler {

    companion object {
        const val DEPARTURE_WINDOW_MS = TravelNotificationController.DEPARTURE_WINDOW_MS
        const val GRACE_PERIOD_MS = TravelNotificationController.GRACE_PERIOD_MS

        // Distinct request codes for the two alarms of the same trip.
        private const val REQUEST_REMINDER_OFFSET = 0
        private const val REQUEST_CLEANUP_OFFSET = 1_000_000

        data class PlannedAlarm(
            val itemId: Int,
            val triggerAtMillis: Long,
            val requestCodeOffset: Int,
            val action: String,
            val path: ExactAlarmCompat.Path
        )

        /**
         * Pure planner: used trips are skipped, everything else is scheduled on either the exact
         * path or the inexact fallback. Never returns an empty plan just because exact alarms
         * are denied.
         */
        fun planAlarms(
            item: TravelInfoItem,
            now: Long,
            hasExactPermission: Boolean
        ): List<PlannedAlarm> {
            if (item.isUsed) return emptyList()
            val path = ExactAlarmCompat.path(hasExactPermission)
            val planned = mutableListOf<PlannedAlarm>()
            val reminderTime = item.departureTime - DEPARTURE_WINDOW_MS
            if (reminderTime > now) {
                planned.add(
                    PlannedAlarm(
                        item.id,
                        reminderTime,
                        REQUEST_REMINDER_OFFSET,
                        TravelAlarmReceiver.ACTION_REMINDER,
                        path
                    )
                )
            }
            val cleanupTime = item.departureTime + GRACE_PERIOD_MS
            if (cleanupTime > now) {
                planned.add(
                    PlannedAlarm(
                        item.id,
                        cleanupTime,
                        REQUEST_CLEANUP_OFFSET,
                        TravelAlarmReceiver.ACTION_CLEANUP,
                        path
                    )
                )
            }
            return planned
        }
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun hasPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return alarmManager.canScheduleExactAlarms()
    }

    override fun scheduleReminder(item: TravelInfoItem) {
        cancelReminder(item.id)
        if (item.isUsed) return
        val now = System.currentTimeMillis()
        // Already inside T-30: do not wait for an alarm whose trigger is in the past.
        if (TravelLiveUpdateGate.shouldPostNow(item, now, suppressionRepository.isSuppressed(item.id))) {
            notificationController.postTripLiveUpdate(item)
        }
        val planned = planAlarms(item, now, hasPermission())
        for (alarm in planned) {
            scheduleAlarm(alarm)
        }
    }

    private fun scheduleAlarm(alarm: PlannedAlarm) {
        val intent = TravelAlarmReceiver.createIntent(context, alarm.itemId, alarm.action)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.itemId + alarm.requestCodeOffset,
            intent,
            PendingIntent_MUTABLE_FLAGS
        )
        ExactAlarmCompat.schedule(
            alarmManager = alarmManager,
            triggerAtMillis = alarm.triggerAtMillis,
            pendingIntent = pendingIntent,
            exact = alarm.path == ExactAlarmCompat.Path.EXACT
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

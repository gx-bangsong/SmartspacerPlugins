package com.kieronquinn.app.smartspacer.plugin.shared.permissions

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build

/**
 * Exact-alarm helpers. `SCHEDULE_EXACT_ALARM` is a special (not runtime) permission and must
 * never be requested via `requestPermissions()`. When the user has not granted it, schedulers
 * must still set an inexact alarm so reminders are not silently dropped.
 */
object ExactAlarmCompat {

    enum class Path {
        EXACT,
        INEXACT_FALLBACK
    }

    fun path(hasExactPermission: Boolean): Path {
        return if (hasExactPermission) Path.EXACT else Path.INEXACT_FALLBACK
    }

    fun hasPermission(sdkInt: Int, canScheduleExactAlarms: Boolean): Boolean {
        return sdkInt < Build.VERSION_CODES.S || canScheduleExactAlarms
    }

    fun hasPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            ?: return false
        return alarmManager.canScheduleExactAlarms()
    }

    /**
     * Schedules [triggerAtMillis] using [AlarmManager.setExactAndAllowWhileIdle] when exact
     * alarms are allowed, otherwise [AlarmManager.setAndAllowWhileIdle] so the reminder still
     * fires (possibly a few minutes off).
     */
    fun schedule(
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        pendingIntent: PendingIntent,
        exact: Boolean,
        type: Int = AlarmManager.RTC_WAKEUP
    ) {
        if (exact) {
            alarmManager.setExactAndAllowWhileIdle(type, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(type, triggerAtMillis, pendingIntent)
        }
    }
}

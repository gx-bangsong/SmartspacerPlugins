package com.kieronquinn.app.smartspacer.plugin.shared.permissions

/**
 * Plugin-provided hook invoked after the user grants (or the system broadcasts a change to)
 * the exact-alarm special permission. Implementations must call the plugin scheduler's
 * `rescheduleAll()` (or equivalent) so existing reminders pick up the exact path.
 */
fun interface ExactAlarmRescheduler {
    suspend fun rescheduleAll()
}

/**
 * Optional hook for SMS denial: plugins that auto-parse SMS should turn that feature off,
 * leaving share / manual paste available.
 */
fun interface SmsPermissionFallback {
    fun onSmsPermissionDenied()
}

/**
 * Coordinates `rescheduleAll()` when exact-alarm permission is granted from settings or the
 * system broadcast. Extracted so the grant/broadcast contract can be unit-tested without an
 * Activity.
 */
class ExactAlarmPermissionHandler(
    private val rescheduler: ExactAlarmRescheduler
) {
    /**
     * Called from `onResume()` after the user returns from
     * [android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM]. Only reschedules when
     * the permission is now granted.
     */
    suspend fun onResumeRecheck(nowGranted: Boolean) {
        if (nowGranted) {
            rescheduler.rescheduleAll()
        }
    }

    /**
     * Called from the manifest receiver for
     * [android.app.AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED].
     * Always reschedules: grant → exact path, revoke → inexact fallback.
     */
    suspend fun onPermissionStateChanged() {
        rescheduler.rescheduleAll()
    }
}

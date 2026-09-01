package com.kieronquinn.app.smartspacer.plugin.shared.permissions

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

/**
 * Manifest receiver for [AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED].
 * Plugins that schedule exact alarms register a concrete subclass in their own manifest so
 * plugins that do not use alarms never receive the broadcast.
 */
abstract class ExactAlarmPermissionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED) {
            return
        }
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val rescheduler = GlobalContext.getOrNull()?.getOrNull<ExactAlarmRescheduler>()
                if (rescheduler != null) {
                    ExactAlarmPermissionHandler(rescheduler).onPermissionStateChanged()
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}

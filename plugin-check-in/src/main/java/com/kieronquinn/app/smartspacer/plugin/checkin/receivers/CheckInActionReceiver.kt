package com.kieronquinn.app.smartspacer.plugin.checkin.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.kieronquinn.app.smartspacer.plugin.checkin.data.CheckInDao
import com.kieronquinn.app.smartspacer.plugin.checkin.data.CheckInItem
import com.kieronquinn.app.smartspacer.plugin.checkin.providers.CheckInProvider
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Notification actions for the check-in reminder: "已打卡" records the punch for today (start or
 * end depending on the reminder type), cancels the notification and refreshes the Smartspacer
 * target — the end condition of the reminder.
 */
class CheckInActionReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        const val ACTION_PUNCH = "com.kieronquinn.app.smartspacer.plugin.checkin.ACTION_PUNCH"
        const val EXTRA_TYPE = "reminder_type"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }

    private val checkInDao by inject<CheckInDao>()

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(EXTRA_TYPE) ?: return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val now = System.currentTimeMillis()
                val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(now))
                val record = checkInDao.getByDate(todayDate)

                val updated = when (type) {
                    CheckInAlarmReceiver.TYPE_START -> {
                        if (record == null) {
                            CheckInItem(date = todayDate, checkInTime = now, checkOutTime = null)
                        } else {
                            record.copy(checkInTime = now)
                        }
                    }
                    CheckInAlarmReceiver.TYPE_END -> {
                        if (record == null) {
                            CheckInItem(date = todayDate, checkInTime = null, checkOutTime = now)
                        } else {
                            record.copy(checkOutTime = now)
                        }
                    }
                    else -> null
                }

                if (updated != null) {
                    checkInDao.insert(updated)
                    SmartspacerTargetProvider.notifyChange(context, CheckInProvider::class.java)
                }

                val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
                if (notificationId != -1) {
                    NotificationManagerCompat.from(context).cancel(notificationId)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}

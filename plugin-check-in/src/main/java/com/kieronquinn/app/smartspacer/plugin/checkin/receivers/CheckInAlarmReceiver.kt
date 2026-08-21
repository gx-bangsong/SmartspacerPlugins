package com.kieronquinn.app.smartspacer.plugin.checkin.receivers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.kieronquinn.app.smartspacer.plugin.checkin.R
import com.kieronquinn.app.smartspacer.plugin.checkin.data.CheckInDao
import com.kieronquinn.app.smartspacer.plugin.checkin.providers.CheckInProvider
import com.kieronquinn.app.smartspacer.plugin.checkin.repositories.CheckInSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.checkin.repositories.CheckInScheduler
import com.kieronquinn.app.smartspacer.plugin.checkin.ui.activities.CheckInActionActivity
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.verifySecurity
import com.kieronquinn.app.smartspacer.sdk.utils.applySecurity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fires at the configured punch reminder time: shows a high-priority notification with a
 * "已打卡" end action (records the punch, cancels the notification) and refreshes the Smartspacer
 * target. This is a static daily reminder, which the official Live Update guidance classifies as
 * inappropriate for promotion — it therefore stays a regular (non-ongoing) notification.
 */
class CheckInAlarmReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        const val TYPE_START = "start"
        const val TYPE_END = "end"
        private const val EXTRA_TYPE = "reminder_type"
        private const val CHANNEL_ID = "check_in_reminders"
        const val NOTIFICATION_ID_START = 1001
        const val NOTIFICATION_ID_END = 1002

        fun createIntent(context: Context, type: String): Intent {
            return Intent(context, CheckInAlarmReceiver::class.java).apply {
                putExtra(EXTRA_TYPE, type)
                applySecurity(context)
            }
        }
    }

    private val checkInDao by inject<CheckInDao>()
    private val settingsRepository by inject<CheckInSettingsRepository>()
    private val scheduler by inject<CheckInScheduler>()

    override fun onReceive(context: Context, intent: Intent) {
        intent.verifySecurity(context)
        val type = intent.getStringExtra(EXTRA_TYPE) ?: return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val record = checkInDao.getByDate(todayDate)
                val checkInOnly = settingsRepository.checkInOnly.first()

                val shouldNotify = when (type) {
                    TYPE_START -> record?.checkInTime == null
                    // 仅上班打卡模式下即使有残留的下班闹钟也不提醒
                    TYPE_END -> !checkInOnly && record?.checkOutTime == null
                    else -> false
                }

                if (shouldNotify) {
                    showNotification(context, type)
                }

                // 到点后立即刷新 Smartspace 卡片，让“到点未打卡”的状态显示出来
                SmartspacerTargetProvider.notifyChange(context, CheckInProvider::class.java)

                // Reschedule for next occurrence
                scheduler.scheduleDailyAlarms()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun showNotification(context: Context, type: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_title),
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val userText = settingsRepository.customReminderText.first()
        val finalContent = if (type == TYPE_START) {
            userText.ifBlank { context.getString(R.string.default_reminder_text) }
        } else {
            context.getString(R.string.default_reminder_text_end)
        }

        val notificationId = if (type == TYPE_START) NOTIFICATION_ID_START else NOTIFICATION_ID_END

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(finalContent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(Notification.CATEGORY_REMINDER)
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .setContentIntent(punchContentIntent(context, type))
            .addAction(
                R.drawable.ic_launcher_foreground,
                context.getString(if (type == TYPE_START) R.string.notification_action_punch_in else R.string.notification_action_punch_out),
                punchActionIntent(context, type, notificationId)
            )
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun punchActionIntent(context: Context, type: String, notificationId: Int): PendingIntent {
        val intent = Intent(context, CheckInActionReceiver::class.java).apply {
            action = CheckInActionReceiver.ACTION_PUNCH
            putExtra(CheckInActionReceiver.EXTRA_TYPE, type)
            putExtra(CheckInActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        return PendingIntent.getBroadcast(
            context,
            if (type == TYPE_START) 3001 else 3002,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun punchContentIntent(context: Context, type: String): PendingIntent {
        val intent = Intent(context, CheckInActionActivity::class.java)
        return PendingIntent.getActivity(
            context,
            if (type == TYPE_START) 4001 else 4002,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

package com.kieronquinn.app.smartspacer.plugin.checkin.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.kieronquinn.app.smartspacer.plugin.checkin.R
import com.kieronquinn.app.smartspacer.plugin.checkin.data.CheckInDao
import com.kieronquinn.app.smartspacer.plugin.checkin.repositories.CheckInSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.checkin.repositories.CheckInScheduler
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

class CheckInAlarmReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        const val TYPE_START = "start"
        const val TYPE_END = "end"
        private const val EXTRA_TYPE = "reminder_type"
        private const val CHANNEL_ID = "check_in_reminders"

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

                val shouldNotify = when (type) {
                    TYPE_START -> record?.checkInTime == null
                    TYPE_END -> record?.checkOutTime == null
                    else -> false
                }

                if (shouldNotify) {
                    showNotification(context, type)
                }

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

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(finalContent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(if (type == TYPE_START) 1001 else 1002, notification)
    }
}

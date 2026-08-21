package com.kieronquinn.app.smartspacer.plugin.water.receivers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kieronquinn.app.smartspacer.plugin.water.WaterPlugin
import com.kieronquinn.app.smartspacer.plugin.water.R
import com.kieronquinn.app.smartspacer.plugin.shared.notifications.NotificationIds
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.DialogLauncherActivity
import com.kieronquinn.app.smartspacer.plugin.water.ui.fragments.RecordDrinkFragment
import com.kieronquinn.app.smartspacer.plugin.water.repositories.WaterDataRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Shows the "time to drink" reminder notification with two actions:
 *  - "记录饮水": records a cup (via [WaterActionReceiver], no app UI needed);
 *  - "稍后提醒": snoozes the reminder.
 *
 * The notification ID is a stable function of the reminder timestamp (namespace
 * `water`), so re-scheduling / snoozing replaces the old notification instead of stacking cards.
 */
class WaterReminderReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        const val EXTRA_REMINDER_TIME = "extra_reminder_time"
    }

    private val waterDataRepository by inject<WaterDataRepository>()

    override fun onReceive(context: Context, intent: Intent) {
        val reminderTime = intent.getLongExtra(EXTRA_REMINDER_TIME, -1L)
        if (reminderTime == -1L || reminderTime > System.currentTimeMillis()) {
            return
        }

        ensureChannel(context)
        val notification = createNotification(context, reminderTime)
        NotificationManagerCompat.from(context).notify(notificationIdFor(reminderTime), notification)
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(
                NotificationChannel(
                    WaterPlugin.NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }
    }

    private fun notificationIdFor(reminderTime: Long): Int =
        NotificationIds.forEntity(NotificationIds.NAMESPACE_WATER, reminderTime)

    private fun createNotification(context: Context, reminderTime: Long): Notification {
        val title = context.getString(R.string.notification_drink_title)
        val content = context.getString(R.string.notification_drink_content)

        val intent = Intent(context, DialogLauncherActivity::class.java).apply {
            putExtra(DialogLauncherActivity.EXTRA_FRAGMENT_CLASS, RecordDrinkFragment::class.java.name)
            putExtra("amount", waterDataRepository.cupMl)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NotificationIds.forEntity("water_content", reminderTime),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationId = notificationIdFor(reminderTime)

        return NotificationCompat.Builder(context, WaterPlugin.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(Notification.CATEGORY_REMINDER)
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_launcher_foreground,
                context.getString(R.string.notification_action_drink),
                drinkIntent(context, reminderTime, notificationId)
            )
            .addAction(
                R.drawable.ic_launcher_foreground,
                context.getString(R.string.notification_action_snooze),
                snoozeIntent(context, reminderTime, notificationId)
            )
            .build()
    }

    private fun drinkIntent(context: Context, reminderTime: Long, notificationId: Int): PendingIntent {
        val intent = Intent(context, WaterActionReceiver::class.java).apply {
            action = WaterActionReceiver.ACTION_DRINK
            putExtra(WaterActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(WaterActionReceiver.EXTRA_AMOUNT, waterDataRepository.cupMl)
        }
        return PendingIntent.getBroadcast(
            context,
            NotificationIds.forEntity("water_drink_action", reminderTime),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun snoozeIntent(context: Context, reminderTime: Long, notificationId: Int): PendingIntent {
        val intent = Intent(context, WaterActionReceiver::class.java).apply {
            action = WaterActionReceiver.ACTION_SNOOZE
            putExtra(WaterActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        return PendingIntent.getBroadcast(
            context,
            NotificationIds.forEntity("water_snooze_action", reminderTime),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

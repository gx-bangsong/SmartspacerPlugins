package com.kieronquinn.app.smartspacer.plugin.water.receivers

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kieronquinn.app.smartspacer.plugin.water.R
import com.kieronquinn.app.smartspacer.plugin.water.WaterPlugin
import com.kieronquinn.app.smartspacer.plugin.water.ui.activities.SettingsActivity

class WaterReminderReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "WaterReminderReceiver"
        const val EXTRA_REMINDER_TIME = "extra_reminder_time"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Water reminder alarm received")
        val reminderTime = intent.getLongExtra(EXTRA_REMINDER_TIME, 0L)

        val markAsDrunkIntent = Intent(context, WaterActionReceiver::class.java).apply {
            action = WaterActionReceiver.ACTION_MARK_AS_DRUNK
        }
        val markAsDrunkPendingIntent: PendingIntent =
            PendingIntent.getBroadcast(context, 0, markAsDrunkIntent, PendingIntent.FLAG_IMMUTABLE)

        val snoozeIntent = Intent(context, WaterActionReceiver::class.java).apply {
            action = WaterActionReceiver.ACTION_SNOOZE
            putExtra(EXTRA_REMINDER_TIME, reminderTime)
        }
        val snoozePendingIntent: PendingIntent =
            PendingIntent.getBroadcast(context, 1, snoozeIntent, PendingIntent.FLAG_IMMUTABLE)

        val openAppIntent = Intent(context, SettingsActivity::class.java)
        val openAppPendingIntent: PendingIntent =
            PendingIntent.getActivity(context, 2, openAppIntent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, WaterPlugin.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with a proper icon
            .setContentTitle("Time for water")
            .setContentText("Stay hydrated!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openAppPendingIntent)
            .addAction(R.drawable.ic_add, "Mark as Drunk", markAsDrunkPendingIntent)
            .addAction(R.drawable.ic_add, "Snooze", snoozePendingIntent) // Replace with a proper icon
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }

}
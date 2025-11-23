package com.kieronquinn.app.smartspacer.plugin.water.receivers

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kieronquinn.app.smartspacer.plugin.water.WaterPlugin
import com.kieronquinn.app.smartspacer.plugin.water.R
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.DialogLauncherActivity
import com.kieronquinn.app.smartspacer.plugin.water.ui.fragments.RecordDrinkFragment
import com.kieronquinn.app.smartspacer.plugin.water.repositories.WaterDataRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

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

        val notification = createNotification(context, reminderTime)
        NotificationManagerCompat.from(context).notify(reminderTime.toInt(), notification)
    }

    private fun createNotification(context: Context, reminderTime: Long): Notification {
        val title = "Time for a glass of water!"
        val content = "Stay hydrated!"

        val intent = Intent(context, DialogLauncherActivity::class.java).apply {
            putExtra(DialogLauncherActivity.EXTRA_FRAGMENT_CLASS, RecordDrinkFragment::class.java.name)
            putExtra("amount", waterDataRepository.cupMl)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderTime.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, WaterPlugin.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
    }
}

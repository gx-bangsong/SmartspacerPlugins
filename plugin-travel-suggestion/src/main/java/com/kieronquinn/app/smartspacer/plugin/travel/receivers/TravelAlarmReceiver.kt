package com.kieronquinn.app.smartspacer.plugin.travel.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.kieronquinn.app.smartspacer.plugin.travel.R
import com.kieronquinn.app.smartspacer.plugin.travel.data.TravelInfoDao
import com.kieronquinn.app.smartspacer.plugin.travel.repositories.TravelScheduler
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.verifySecurity
import com.kieronquinn.app.smartspacer.sdk.utils.applySecurity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TravelAlarmReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        private const val EXTRA_TRAVEL_ITEM_ID = "travel_item_id"
        private const val CHANNEL_ID = "travel_reminders"

        fun createIntent(context: Context, travelItemId: Int): Intent {
            return Intent(context, TravelAlarmReceiver::class.java).apply {
                putExtra(EXTRA_TRAVEL_ITEM_ID, travelItemId)
                applySecurity(context)
            }
        }
    }

    private val travelInfoDao by inject<TravelInfoDao>()
    private val travelScheduler by inject<TravelScheduler>()

    override fun onReceive(context: Context, intent: Intent) {
        intent.verifySecurity(context)
        val travelItemId = intent.getIntExtra(EXTRA_TRAVEL_ITEM_ID, -1)
        if (travelItemId == -1) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val item = travelInfoDao.getById(travelItemId)
                if (item != null && !item.isUsed) {
                    showNotification(context, item)
                    travelScheduler.scheduleReminder(item)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context, item: com.kieronquinn.app.smartspacer.plugin.travel.data.TravelInfoItem) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_title),
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val timeString = sdf.format(Date(item.departureTime))

        val content = context.getString(
            R.string.notification_content,
            item.trainNumber,
            item.departureStation,
            timeString
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(item.id, notification)
    }
}

package com.kieronquinn.app.smartspacer.plugin.water.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.kieronquinn.app.smartspacer.plugin.shared.notifications.LiveUpdateNotificationController
import com.kieronquinn.app.smartspacer.plugin.shared.notifications.LiveUpdateSpec
import com.kieronquinn.app.smartspacer.plugin.shared.notifications.NotificationIds
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.DialogLauncherActivity
import com.kieronquinn.app.smartspacer.plugin.water.R
import com.kieronquinn.app.smartspacer.plugin.water.repositories.WaterDataRepository
import com.kieronquinn.app.smartspacer.plugin.water.ui.fragments.RecordDrinkFragment
import java.time.LocalDate
import java.time.LocalTime

/**
 * Daily hydration Live Update. The status chip is the ml fraction; the notification
 * is upgraded in place as drinks are recorded.
 */
class WaterLiveUpdatePublisher(
    context: Context,
    private val waterDataRepository: WaterDataRepository
) {
    private val context = context.applicationContext
    private val controller = LiveUpdateNotificationController(context)

    companion object {
        const val CHANNEL_ID = "water_live_progress"
        private const val ENTITY_ID = 1L
    }

    suspend fun publish() {
        val drinks = waterDataRepository.getDrinksForDate(LocalDate.now())
        val progress = WaterProgress(
            drankMl = drinks.sumOf { it.amount },
            goalMl = waterDataRepository.dailyGoalMl,
            cupMl = waterDataRepository.cupMl
        )
        val now = LocalTime.now()
        val start = LocalTime.ofSecondOfDay(waterDataRepository.activeStartMinutes * 60L)
        val end = LocalTime.ofSecondOfDay(waterDataRepository.activeEndMinutes * 60L)
        val inWindow = !now.isBefore(start) && !now.isAfter(end)
        if (!inWindow || progress.reached) {
            controller.cancel(notificationId())
            return
        }
        controller.post(
            LiveUpdateSpec(
                channelId = CHANNEL_ID,
                channelNameRes = R.string.notification_live_channel,
                channelImportance = NotificationManager.IMPORTANCE_DEFAULT,
                notificationId = notificationId(),
                smallIconRes = R.drawable.ic_launcher_greyscale,
                contentTitle = context.getString(R.string.notification_live_title),
                contentText = context.getString(
                    R.string.notification_live_content,
                    progress.drankMl,
                    progress.goalMl,
                    progress.percent
                ),
                shortCriticalText = progress.capsule,
                progress = progress.drankMl.coerceAtMost(progress.goalMl.coerceAtLeast(1)),
                progressMax = progress.goalMl.coerceAtLeast(1),
                ongoing = true,
                requestPromoted = true,
                autoCancel = false,
                priority = NotificationCompat.PRIORITY_DEFAULT,
                category = Notification.CATEGORY_STATUS,
                contentIntent = contentIntent(),
                visibility = Notification.VISIBILITY_PUBLIC
            )
        )
    }

    fun cancel() {
        controller.cancel(notificationId())
    }

    private fun notificationId(): Int =
        NotificationIds.forEntity(NotificationIds.NAMESPACE_WATER_PROGRESS, ENTITY_ID)

    private fun contentIntent(): PendingIntent {
        val intent = Intent(context, DialogLauncherActivity::class.java).apply {
            putExtra(DialogLauncherActivity.EXTRA_FRAGMENT_CLASS, RecordDrinkFragment::class.java.name)
            putExtra("amount", waterDataRepository.cupMl)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        return PendingIntent.getActivity(
            context,
            NotificationIds.forEntity("water_live_content", ENTITY_ID),
            intent,
            flags
        )
    }
}

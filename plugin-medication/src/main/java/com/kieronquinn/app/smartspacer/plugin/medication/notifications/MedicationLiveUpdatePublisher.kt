package com.kieronquinn.app.smartspacer.plugin.medication.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.kieronquinn.app.smartspacer.plugin.medication.R
import com.kieronquinn.app.smartspacer.plugin.medication.data.DoseHistory
import com.kieronquinn.app.smartspacer.plugin.medication.data.DoseHistoryDao
import com.kieronquinn.app.smartspacer.plugin.medication.data.Medication
import com.kieronquinn.app.smartspacer.plugin.medication.data.MedicationDao
import com.kieronquinn.app.smartspacer.plugin.medication.ui.fragments.RecordDoseFragment
import com.kieronquinn.app.smartspacer.plugin.shared.notifications.LiveUpdateNotificationController
import com.kieronquinn.app.smartspacer.plugin.shared.notifications.LiveUpdateSpec
import com.kieronquinn.app.smartspacer.plugin.shared.notifications.NotificationIds
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.DialogLauncherActivity
import kotlinx.coroutines.flow.first
import java.util.Calendar

/**
 * Multi-dose medication Live Update. The chip is taken/scheduled for today, e.g. `2/4`.
 * Single-dose reminders stay as regular notifications.
 */
class MedicationLiveUpdatePublisher(
    context: Context,
    private val medicationDao: MedicationDao,
    private val doseHistoryDao: DoseHistoryDao
) {
    private val context = context.applicationContext
    private val controller = LiveUpdateNotificationController(context)

    companion object {
        const val CHANNEL_ID = "medication_live_progress"
    }

    suspend fun publishAll() {
        val now = System.currentTimeMillis()
        for (medication in medicationDao.getAll().first()) {
            publish(medication, now)
        }
    }

    suspend fun publish(medication: Medication, now: Long = System.currentTimeMillis()) {
        if (!medication.enabled || !MedicationProgress.isMultiDoseToday(medication)) {
            controller.cancel(notificationId(medication.id))
            return
        }
        val dayStart = startOfDay(now)
        val dayEnd = dayStart + 24L * 60 * 60 * 1000
        val taken = doseHistoryDao.getForMedicationBetween(medication.id, dayStart, dayEnd)
            .count { it.status == DoseHistory.Status.TAKEN }
        val scheduled = MedicationProgress.scheduledToday(medication)
        if (taken >= scheduled) {
            controller.cancel(notificationId(medication.id))
            return
        }
        val capsule = MedicationProgress.capsule(taken, scheduled)
        controller.post(
            LiveUpdateSpec(
                channelId = CHANNEL_ID,
                channelNameRes = R.string.notification_live_channel,
                channelImportance = NotificationManager.IMPORTANCE_DEFAULT,
                notificationId = notificationId(medication.id),
                smallIconRes = R.drawable.ic_launcher_greyscale,
                contentTitle = medication.name,
                contentText = context.getString(
                    R.string.notification_live_content,
                    taken,
                    scheduled
                ),
                shortCriticalText = capsule,
                progress = taken,
                progressMax = scheduled.coerceAtLeast(1),
                ongoing = true,
                requestPromoted = true,
                autoCancel = false,
                priority = NotificationCompat.PRIORITY_DEFAULT,
                category = Notification.CATEGORY_STATUS,
                contentIntent = contentIntent(medication.id),
                visibility = Notification.VISIBILITY_PRIVATE,
                publicVersionTitle = context.getString(R.string.notification_live_public_title),
                publicVersionText = capsule
            )
        )
    }

    fun cancel(medicationId: Int) {
        controller.cancel(notificationId(medicationId))
    }

    private fun notificationId(medicationId: Int): Int =
        NotificationIds.forEntity(NotificationIds.NAMESPACE_MEDICATION_PROGRESS, medicationId.toLong())

    private fun contentIntent(medicationId: Int): PendingIntent {
        val intent = Intent(context, DialogLauncherActivity::class.java).apply {
            putExtra(DialogLauncherActivity.EXTRA_FRAGMENT_CLASS, RecordDoseFragment::class.java.name)
            putExtra("medicationId", medicationId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        return PendingIntent.getActivity(
            context,
            NotificationIds.forEntity("medication_live_content", medicationId.toLong()),
            intent,
            flags
        )
    }

    private fun startOfDay(now: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}

package com.kieronquinn.app.smartspacer.plugin.medication.receivers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.kieronquinn.app.smartspacer.plugin.medication.R
import com.kieronquinn.app.smartspacer.plugin.medication.data.MedicationDao
import com.kieronquinn.app.smartspacer.plugin.medication.providers.MedicationProvider
import com.kieronquinn.app.smartspacer.plugin.medication.repositories.MedicationScheduler
import com.kieronquinn.app.smartspacer.plugin.shared.notifications.NotificationIds
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.DialogLauncherActivity
import com.kieronquinn.app.smartspacer.plugin.medication.ui.fragments.RecordDoseFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.verifySecurity
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.applySecurity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Fires at the scheduled dose time: shows the dose reminder notification (with "已服用" and
 * "稍后提醒" end actions) using the medication's stable notification ID, refreshes the Smartspacer
 * target, then reschedules the next dose. The T-60min "warning window" alarm only refreshes the
 * target; the notification itself appears when the dose time is reached.
 *
 * This is a periodic static reminder, which the official Live Update guidance classifies as
 * inappropriate for promotion — it stays a regular notification.
 */
class MedicationAlarmReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        private const val EXTRA_MEDICATION_ID = "medication_id"
        private const val CHANNEL_ID = "medication_reminders"

        fun createIntent(context: Context, medicationId: Int): Intent {
            return Intent(context, MedicationAlarmReceiver::class.java).apply {
                putExtra(EXTRA_MEDICATION_ID, medicationId)
                applySecurity(context)
            }
        }
    }

    private val medicationDao by inject<MedicationDao>()
    private val medicationScheduler by inject<MedicationScheduler>()

    override fun onReceive(context: Context, intent: Intent) {
        intent.verifySecurity(context)
        val medicationId = intent.getIntExtra(EXTRA_MEDICATION_ID, -1)
        if (medicationId == -1) return

        SmartspacerTargetProvider.notifyChange(context, MedicationProvider::class.java)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val medication = medicationDao.getById(medicationId)
                if (medication != null) {
                    val now = System.currentTimeMillis()
                    if (now >= medication.nextDoseTs) {
                        // 到达服药时间：显示提醒通知
                        showNotification(context, medicationId, medication.name, medication.dosage)
                    }
                    medicationScheduler.scheduleAlarm(medication)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context, medicationId: Int, name: String, dosage: String?) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationId = NotificationIds.forEntity(NotificationIds.NAMESPACE_MEDICATION, medicationId.toLong())

        val contentIntent = Intent(context, DialogLauncherActivity::class.java).apply {
            putExtra(DialogLauncherActivity.EXTRA_FRAGMENT_CLASS, RecordDoseFragment::class.java.name)
            putExtra("medicationId", medicationId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            NotificationIds.forEntity("medication_content", medicationId.toLong()),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_pill)
            .setContentTitle(context.getString(R.string.notification_dose_title, name))
            .setContentText(
                if (dosage.isNullOrBlank()) context.getString(R.string.notification_dose_content)
                else context.getString(R.string.notification_dose_content_with_dosage, dosage)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(Notification.CATEGORY_REMINDER)
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(
                R.drawable.ic_pill,
                context.getString(R.string.notification_action_taken),
                actionIntent(context, medicationId, notificationId, MedicationActionReceiver.ACTION_TAKEN)
            )
            .addAction(
                R.drawable.ic_pill,
                context.getString(R.string.notification_action_snooze),
                actionIntent(context, medicationId, notificationId, MedicationActionReceiver.ACTION_SNOOZE)
            )
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun actionIntent(
        context: Context,
        medicationId: Int,
        notificationId: Int,
        action: String
    ): PendingIntent {
        val intent = Intent(context, MedicationActionReceiver::class.java).apply {
            this.action = action
            putExtra(MedicationActionReceiver.EXTRA_MEDICATION_ID, medicationId)
            putExtra(MedicationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        return PendingIntent.getBroadcast(
            context,
            NotificationIds.forEntity("medication_action_$action", medicationId.toLong()),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

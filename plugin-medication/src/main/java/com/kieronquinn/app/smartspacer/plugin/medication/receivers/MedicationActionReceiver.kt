package com.kieronquinn.app.smartspacer.plugin.medication.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.kieronquinn.app.smartspacer.plugin.medication.data.DoseHistory
import com.kieronquinn.app.smartspacer.plugin.medication.data.DoseHistoryDao
import com.kieronquinn.app.smartspacer.plugin.medication.data.MedicationDao
import com.kieronquinn.app.smartspacer.plugin.medication.data.MedicationUtils
import com.kieronquinn.app.smartspacer.plugin.medication.providers.MedicationProvider
import com.kieronquinn.app.smartspacer.plugin.medication.repositories.MedicationScheduler
import com.kieronquinn.app.smartspacer.plugin.medication.work.MedicationWorker
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Notification actions for the dose reminder:
 *  - [ACTION_TAKEN]: records the dose as taken, advances to the next dose and reschedules the
 *    alarm — the reminder's end condition;
 *  - [ACTION_SNOOZE]: moves the next dose to "now + snooze" and reschedules.
 * Both cancel the current notification.
 */
class MedicationActionReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        const val ACTION_TAKEN = "com.kieronquinn.app.smartspacer.plugin.medication.ACTION_TAKEN"
        const val ACTION_SNOOZE = "com.kieronquinn.app.smartspacer.plugin.medication.ACTION_SNOOZE"
        const val EXTRA_MEDICATION_ID = "medication_id"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val SNOOZE_MS = 15L * 60 * 1000L
    }

    private val medicationDao by inject<MedicationDao>()
    private val doseHistoryDao by inject<DoseHistoryDao>()
    private val medicationScheduler by inject<MedicationScheduler>()

    override fun onReceive(context: Context, intent: Intent) {
        val medicationId = intent.getIntExtra(EXTRA_MEDICATION_ID, -1)
        if (medicationId == -1) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val medication = medicationDao.getById(medicationId)
                if (medication != null) {
                    when (intent.action) {
                        ACTION_TAKEN -> {
                            doseHistoryDao.insert(
                                DoseHistory(
                                    medicationId = medication.id,
                                    timestamp = System.currentTimeMillis(),
                                    status = DoseHistory.Status.TAKEN
                                )
                            )
                            val updated = medication.copy(
                                nextDoseTs = MedicationUtils.calculateNextDose(medication)
                            )
                            medicationDao.update(updated)
                            medicationScheduler.scheduleAlarm(updated)
                        }
                        ACTION_SNOOZE -> {
                            val updated = medication.copy(
                                nextDoseTs = System.currentTimeMillis() + SNOOZE_MS
                            )
                            medicationDao.update(updated)
                            medicationScheduler.scheduleAlarm(updated)
                        }
                    }
                    SmartspacerTargetProvider.notifyChange(context, MedicationProvider::class.java)
                    MedicationWorker.enqueueImmediate(context)
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

package com.kieronquinn.app.smartspacer.plugin.medicationreminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.MedicationRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MedicationActionReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        const val ACTION_MARK_AS_TAKEN = "ACTION_MARK_AS_TAKEN"
        const val ACTION_SNOOZE = "ACTION_SNOOZE"
        const val EXTRA_MEDICATION_ID = "extra_medication_id"
        const val EXTRA_DOSE_TIME = "extra_dose_time"
    }

    private val repository: MedicationRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val medicationId = intent.getIntExtra(EXTRA_MEDICATION_ID, -1)
        if (medicationId == -1) return

        val doseTime = intent.getLongExtra(EXTRA_DOSE_TIME, -1)

        when (intent.action) {
            ACTION_MARK_AS_TAKEN -> {
                if (doseTime != -1L) {
                    GlobalScope.launch {
                        repository.insert(
                            com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.TakenDose(
                                medicationId = medicationId,
                                doseTime = doseTime,
                                takenTime = System.currentTimeMillis()
                            )
                        )
                    }
                }
            }
            ACTION_SNOOZE -> {
                val settings: MedicationReminderSettings by inject()
                val snoozeDuration = settings.snoozeDuration
                val workRequest = androidx.work.OneTimeWorkRequestBuilder<com.kieronquinn.app.smartspacer.plugin.medicationreminder.worker.NotificationWorker>()
                    .setInitialDelay(snoozeDuration.toLong(), java.util.concurrent.TimeUnit.MINUTES)
                    .setInputData(intent.extras ?: androidx.work.Data.EMPTY)
                    .build()
                androidx.work.WorkManager.getInstance(context).enqueue(workRequest)
            }
        }
    }
}
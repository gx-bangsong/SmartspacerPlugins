package com.kieronquinn.app.smartspacer.plugin.medicationreminder.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.MedicationRepository
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.SnoozeRepository
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.providers.MedicationReminderProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MedicationActionReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        const val TAG = "MedicationActionRcvr"
        const val ACTION_MARK_AS_TAKEN = "com.kieronquinn.app.smartspacer.plugin.medicationreminder.MARK_AS_TAKEN"
        const val ACTION_SNOOZE = "com.kieronquinn.app.smartspacer.plugin.medicationreminder.SNOOZE"
        const val EXTRA_MEDICATION_ID = "extra_medication_id"
        private const val DEFAULT_SNOOZE_MINUTES = 15L
    }

    private val medicationRepository: MedicationRepository by inject()
    private val snoozeRepository: SnoozeRepository by inject()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val medicationId = intent.getIntExtra(EXTRA_MEDICATION_ID, -1)
        if (medicationId == -1) {
            Log.e(TAG, "Medication ID not found in intent")
            pendingResult.finish()
            return
        }

        scope.launch {
            try {
                when (intent.action) {
                    ACTION_MARK_AS_TAKEN -> {
                        Log.d(TAG, "Marking medication $medicationId as taken")
                        medicationRepository.logDose(medicationId, System.currentTimeMillis())
                    }
                    ACTION_SNOOZE -> {
                        Log.d(TAG, "Snoozing medication $medicationId for $DEFAULT_SNOOZE_MINUTES minutes")
                        snoozeRepository.snooze(medicationId, DEFAULT_SNOOZE_MINUTES)
                    }
                }
                // Notify the provider to refresh its targets
                val refreshIntent = Intent(context, MedicationReminderProvider::class.java).apply {
                    action = MedicationReminderProvider.ACTION_REFRESH
                }
                context.sendBroadcast(refreshIntent)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
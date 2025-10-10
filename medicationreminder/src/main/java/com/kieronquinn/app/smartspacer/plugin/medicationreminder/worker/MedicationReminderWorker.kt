package com.kieronquinn.app.smartspacer.plugin.medicationreminder.worker

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.MedicationDao
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.logic.SchedulingEngine
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.providers.MedicationReminderProvider
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MedicationReminderWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    companion object {
        const val WORKER_TAG = "medication_reminder_worker"
    }

    private val medicationDao: MedicationDao by inject()

    override suspend fun doWork(): Result {
        Log.d(WORKER_TAG, "Medication reminder worker running...")
        val medications = medicationDao.getMedications().first()

        if (medications.isEmpty()) {
            Log.d(WORKER_TAG, "No medications to check.")
            return Result.success()
        }

        // Check if there are any upcoming doses. If so, notify the provider to refresh.
        // The provider itself contains the detailed logic to display the correct reminders.
        val hasUpcomingDoses = medications.any {
            val doseLogs = medicationDao.getDoseLogsForMedication(it.id).first()
            SchedulingEngine.getNextDueDate(it, doseLogs) != null
        }

        if (hasUpcomingDoses) {
            Log.d(WORKER_TAG, "Upcoming doses found. Notifying provider to refresh.")
            notifyProvider()
        } else {
            Log.d(WORKER_TAG, "No upcoming doses found.")
        }

        return Result.success()
    }

    private fun notifyProvider() {
        val intent = Intent(appContext, MedicationReminderProvider::class.java).apply {
            action = MedicationReminderProvider.ACTION_REFRESH
        }
        appContext.sendBroadcast(intent)
    }
}
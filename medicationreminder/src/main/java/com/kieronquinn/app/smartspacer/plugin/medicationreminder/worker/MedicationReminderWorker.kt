package com.kieronquinn.app.smartspacer.plugin.medicationreminder.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.MedicationRepository
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MedicationReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val repository: MedicationRepository by inject()

    override suspend fun doWork(): Result {
        val medications = repository.allMedications.first()
        medications.forEach { medication ->
            val nextDoseTime = getNextDoseTime(medication)
            if (nextDoseTime != null) {
                val delay = nextDoseTime - System.currentTimeMillis()
                if (delay > 0) {
                    val workRequest = androidx.work.OneTimeWorkRequestBuilder<NotificationWorker>()
                        .setInitialDelay(delay, java.util.concurrent.TimeUnit.MILLISECONDS)
                        .setInputData(
                            androidx.work.Data.Builder()
                                .putInt("medication_id", medication.id)
                                .putString("medication_name", medication.name)
                                .putString("medication_dosage", medication.dosage)
                                .build()
                        )
                        .build()
                    androidx.work.WorkManager.getInstance(applicationContext).enqueue(workRequest)
                }
            }
        }
        return Result.success()
    }

    private suspend fun getNextDoseTime(medication: com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.Medication): Long? {
        val now = System.currentTimeMillis()
        if (medication.endDate != null && now > medication.endDate) {
            return null // Medication has expired
        }

        val takenDoses = repository.getTakenDosesForMedication(medication.id).first()
        val calendar = java.util.Calendar.getInstance()

        return when (medication.schedule.type) {
            com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.ScheduleType.EVERY_X_HOURS -> {
                var lastDoseTime = takenDoses.maxOfOrNull { it.takenTime } ?: medication.startDate
                while (lastDoseTime < now) {
                    lastDoseTime += medication.schedule.interval!! * 60 * 60 * 1000
                }
                lastDoseTime
            }
            com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.ScheduleType.SPECIFIC_TIMES -> {
                medication.schedule.times?.map { time ->
                    val (hour, minute) = time.split(":").map { it.toInt() }
                    calendar.set(java.util.Calendar.HOUR_OF_DAY, hour)
                    calendar.set(java.util.Calendar.MINUTE, minute)
                    calendar.set(java.util.Calendar.SECOND, 0)
                    if (calendar.timeInMillis < now) {
                        calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
                    }
                    calendar.timeInMillis
                }?.minOrNull()
            }
            com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.ScheduleType.EVERY_X_DAYS -> {
                var lastDoseTime = takenDoses.maxOfOrNull { it.takenTime } ?: medication.startDate
                while (lastDoseTime < now) {
                    lastDoseTime += medication.schedule.interval!! * 24 * 60 * 60 * 1000
                }
                lastDoseTime
            }
            com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.ScheduleType.SPECIFIC_DAYS_OF_WEEK -> {
                medication.schedule.daysOfWeek?.map { day ->
                    calendar.set(java.util.Calendar.DAY_OF_WEEK, day)
                    if (calendar.timeInMillis < now) {
                        calendar.add(java.util.Calendar.WEEK_OF_YEAR, 1)
                    }
                    calendar.timeInMillis
                }?.minOrNull()
            }
        }
    }
}
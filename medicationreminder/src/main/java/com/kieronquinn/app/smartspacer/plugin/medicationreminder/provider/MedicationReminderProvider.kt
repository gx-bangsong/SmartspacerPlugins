package com.kieronquinn.app.smartspacer.plugin.medicationreminder.provider

import android.app.PendingIntent
import android.content.Intent
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerProvider
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceAction
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.SubListTemplateData
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.MedicationRepository
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.receiver.MedicationActionReceiver
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MedicationReminderProvider : SmartspacerProvider(), KoinComponent {

    private val repository: MedicationRepository by inject()

    override suspend fun getSmartspaceTargets(): List<SmartspaceTarget> {
        val medications = repository.allMedications.first()
        if (medications.isEmpty()) {
            return emptyList()
        }

        val upcomingDoses = medications.mapNotNull { medication ->
            val nextDoseTime = getNextDoseTime(medication)
            if (nextDoseTime != null) {
                medication to nextDoseTime
            } else {
                null
            }
        }.sortedBy { it.second }

        if (upcomingDoses.isEmpty()) {
            return emptyList()
        }

        val subListItems = upcomingDoses.map { (medication, nextDoseTime) ->
            val markAsTakenIntent = Intent(context, MedicationActionReceiver::class.java).apply {
                action = MedicationActionReceiver.ACTION_MARK_AS_TAKEN
                putExtra(MedicationActionReceiver.EXTRA_MEDICATION_ID, medication.id)
                putExtra(MedicationActionReceiver.EXTRA_DOSE_TIME, nextDoseTime)
            }
            val markAsTakenPendingIntent = PendingIntent.getBroadcast(
                context,
                medication.id + MedicationActionReceiver.ACTION_MARK_AS_TAKEN.hashCode(),
                markAsTakenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val snoozeIntent = Intent(context, MedicationActionReceiver::class.java).apply {
                action = MedicationActionReceiver.ACTION_SNOOZE
                putExtra(MedicationActionReceiver.EXTRA_MEDICATION_ID, medication.id)
                putExtra(MedicationActionReceiver.EXTRA_DOSE_TIME, nextDoseTime)
            }
            val snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                medication.id + MedicationActionReceiver.ACTION_SNOOZE.hashCode(),
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            SubListTemplateData.SubListItem(
                text = Text("Take ${it.name} ${it.dosage ?: ""}"),
                tapAction = SmartspaceAction(
                    id = "mark_as_taken_${it.id}",
                    intent = markAsTakenIntent,
                    pendingIntent = markAsTakenPendingIntent,
                    title = "Mark as Taken"
                ),
                secondaryAction = SmartspaceAction(
                    id = "snooze_${it.id}",
                    intent = snoozeIntent,
                    pendingIntent = snoozePendingIntent,
                    title = "Snooze"
                )
            )
        }

        return listOf(
            SmartspaceTarget(
                id = "medication_reminder_list",
                componentName = componentName,
                templateData = SubListTemplateData(
                    title = Text("Medication Reminders"),
                    subListTexts = subListItems
                )
            )
        )
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
                if (medication.schedule.interval == null) return null
                var lastDoseTime = takenDoses.maxOfOrNull { it.takenTime } ?: medication.startDate
                while (lastDoseTime < now) {
                    lastDoseTime += medication.schedule.interval * 60 * 60 * 1000
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
                if (medication.schedule.interval == null) return null
                var lastDoseTime = takenDoses.maxOfOrNull { it.takenTime } ?: medication.startDate
                while (lastDoseTime < now) {
                    lastDoseTime += medication.schedule.interval * 24 * 60 * 60 * 1000
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
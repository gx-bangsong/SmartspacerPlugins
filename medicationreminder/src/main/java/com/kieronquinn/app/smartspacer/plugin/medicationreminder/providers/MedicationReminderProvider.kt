package com.kieronquinn.app.smartspacer.plugin.medicationreminder.providers

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceAction
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.SubcardTemplateData
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.MedicationDao
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.SnoozeRepository
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.logic.SchedulingEngine
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.receivers.MedicationActionReceiver
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.ui.settings.MedicationReminderSettingsActivity
import kotlinx.coroutines.flow.first
import org.koin.core.component.inject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MedicationReminderProvider : SmartspacerTargetProvider() {

    override fun getConfig(smartspacerId: String?): Config {
        return Config(
            label = "Medication Reminders",
            description = "Reminds you to take your medication",
            icon = R.drawable.ic_launcher_foreground
        )
    }

    companion object {
        private const val TAG = "MedicationProvider"
        const val ACTION_REFRESH = "com.kieronquinn.app.smartspacer.plugin.medicationreminder.REFRESH"
        private const val SNOOZE_REQUEST_CODE_OFFSET = 10000
    }

    private val medicationDao: MedicationDao by inject()
    private val snoozeRepository: SnoozeRepository by inject()

    override suspend fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        val medications = medicationDao.getMedications().first()
        if (medications.isEmpty()) {
            return emptyList()
        }

        val now = LocalDateTime.now()
        val upcomingDoses = medications.mapNotNull { medication ->
            val doseLogs = medicationDao.getDoseLogsForMedication(medication.id).first()
            val nextDueDate = SchedulingEngine.getNextDueDate(medication, doseLogs)
            if (nextDueDate != null && !snoozeRepository.isSnoozed(medication.id)) {
                Triple(medication, nextDueDate, nextDueDate.isBefore(now)) // Medication, Time, isMissed
            } else {
                null
            }
        }.sortedBy { it.second }.take(3)

        if (upcomingDoses.isEmpty()) {
            return emptyList()
        }

        val settingsIntent = Intent(context, MedicationReminderSettingsActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val subcards = upcomingDoses.map { (medication, time, isMissed) ->
            createSubcard(medication, time, isMissed, settingsIntent)
        }

        val target = SmartspaceTarget(
            id = "medication_reminder_target",
            componentName = componentName,
            smartspaceTargetId = "medication_reminder_target",
            templateData = SubcardTemplateData(
                subcardInfos = subcards,
                subcardAction = TapAction(intent = settingsIntent)
            ),
            headerAction = SmartspaceAction(
                id = "medication_reminder_header",
                intent = settingsIntent,
                title = "Medication Reminders"
            )
        )

        Log.d(TAG, "Providing ${subcards.size} medication reminders")
        return listOf(target)
    }

    private fun createSubcard(medication: com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.model.Medication, time: LocalDateTime, isMissed: Boolean, settingsIntent: Intent): SubcardTemplateData.SubcardInfo {
        val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
        var doseText = "Take ${medication.name} at ${time.format(timeFormatter)}"
        if (isMissed) {
            doseText += " (Missed)"
        }

        val takenIntent = Intent(context, MedicationActionReceiver::class.java).apply {
            action = MedicationActionReceiver.ACTION_MARK_AS_TAKEN
            putExtra(MedicationActionReceiver.EXTRA_MEDICATION_ID, medication.id)
        }
        val takenPendingIntent = PendingIntent.getBroadcast(context, medication.id, takenIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val takenAction = SmartspaceAction(id = "action_taken_${medication.id}", title = "Mark as Taken", pendingIntent = takenPendingIntent)

        val snoozeIntent = Intent(context, MedicationActionReceiver::class.java).apply {
            action = MedicationActionReceiver.ACTION_SNOOZE
            putExtra(MedicationActionReceiver.EXTRA_MEDICATION_ID, medication.id)
        }
        val snoozeRequestCode = medication.id + SNOOZE_REQUEST_CODE_OFFSET
        val snoozePendingIntent = PendingIntent.getBroadcast(context, snoozeRequestCode, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val snoozeAction = SmartspaceAction(id = "action_snooze_${medication.id}", title = "Snooze", pendingIntent = snoozePendingIntent)

        return SubcardTemplateData.SubcardInfo(
            text = Text(doseText),
            tapAction = TapAction(intent = settingsIntent),
            actions = listOf(takenAction, snoozeAction)
        )
    }

    override fun onReceive(intent: Intent) {
        super.onReceive(intent)
        if (intent.action == ACTION_REFRESH) {
            Log.d(TAG, "Received refresh broadcast, notifying change.")
            notifyChange()
        }
    }
}
package com.kieronquinn.app.smartspacer.plugin.medication.providers

import com.kieronquinn.app.smartspacer.plugin.medication.R
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider

import android.content.ComponentName
import android.content.ComponentName
import android.content.Intent
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceAction
import com.kieronquinn.app.smartspacer.plugin.medication.data.MedicationDao
import com.kieronquinn.app.smartspacer.plugin.medication.ui.activities.RecordDoseActivity
import com.kieronquinn.app.smartspacer.plugin.medication.ui.activities.SettingsActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MedicationProvider : SmartspacerTargetProvider(), KoinComponent {

    private val medicationDao by inject<MedicationDao>()

    override fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        val context = this.context ?: return emptyList()
        val medications = runBlocking { medicationDao.getAll().first() }
        val now = System.currentTimeMillis()

        return medications
            .filter { it.enabled && now >= it.nextDoseTs }
            .map { medication ->
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val time = timeFormat.format(Date(medication.nextDoseTs))
                val title = "${medication.name} ${medication.dosage ?: ""} - Take at $time"

                val intent = Intent(context, RecordDoseActivity::class.java).apply {
                    putExtra("medication_id", medication.id)
                }

                SmartspaceTarget(
                    smartspaceTargetId = "medication_${medication.id}",
                    headerAction = SmartspaceAction(
                        id = "medication_header_${medication.id}",
                        title = title,
                        intent = intent
                    ),
                    featureType = SmartspaceTarget.FEATURE_REMINDER,
                    componentName = ComponentName(context, MedicationProvider::class.java)
                )
            }
    }

    override fun getConfig(smartspacerId: String?): Config {
        return Config(
            label = "Medication Reminder",
            description = "A medication reminder",
            icon = android.graphics.drawable.Icon.createWithResource(context, R.drawable.ic_launcher_foreground),
            configActivity = Intent(context, SettingsActivity::class.java)
        )
    }

    override fun onDismiss(smartspacerId: String, targetId: String): Boolean {
        // This will be handled by the RecordDoseActivity
        return false
    }

}
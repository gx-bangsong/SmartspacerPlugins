package com.kieronquinn.app.smartspacer.plugin.medication.providers

import android.content.ComponentName
import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.medication.R
import com.kieronquinn.app.smartspacer.plugin.medication.data.MedicationDao
import com.kieronquinn.app.smartspacer.plugin.medication.ui.fragments.RecordDoseFragment
import com.kieronquinn.app.smartspacer.plugin.medication.ui.activities.SettingsActivity
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.DialogLauncherActivity
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.graphics.drawable.Icon as AndroidIcon

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

                val intent = Intent(context, DialogLauncherActivity::class.java).apply {
                    putExtra(DialogLauncherActivity.EXTRA_FRAGMENT_CLASS, RecordDoseFragment::class.java.name)
                    putExtra("medication_id", medication.id)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                TargetTemplate.Basic(
                    id = "medication_${medication.id}",
                    componentName = ComponentName(context, MedicationProvider::class.java),
                    featureType = SmartspaceTarget.FEATURE_REMINDER,
                    title = Text(title),
                    subtitle = Text(""),
                    icon = com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon(AndroidIcon.createWithResource(context, R.drawable.ic_launcher_foreground)),
                    onClick = TapAction(intent = intent)
                ).create()
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
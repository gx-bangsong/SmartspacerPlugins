package com.kieronquinn.app.smartspacer.plugin.medication.providers

import android.content.ComponentName
import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.medication.R
import com.kieronquinn.app.smartspacer.plugin.medication.data.MedicationDao
import com.kieronquinn.app.smartspacer.plugin.medication.ui.fragments.RecordDoseFragment
import com.kieronquinn.app.smartspacer.plugin.medication.data.Medication
import com.kieronquinn.app.smartspacer.plugin.medication.data.ScheduleType
import com.kieronquinn.app.smartspacer.plugin.medication.ui.activities.SettingsActivity
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.DialogLauncherActivity
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon as SmartspaceIcon
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar
import android.graphics.drawable.Icon as AndroidIcon

class MedicationProvider : SmartspacerTargetProvider(), KoinComponent {

    private val medicationDao by inject<MedicationDao>()

    override fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        val context = this.context ?: return emptyList()
        val medications = runBlocking { medicationDao.getAll().first() }
        val now = System.currentTimeMillis()

        return medications
            .filter { medication ->
                if (!medication.enabled) return@filter false

                // For specific weekdays, ensure today is one of the allowed days
                if (medication.scheduleType == ScheduleType.SPECIFIC_WEEKDAYS) {
                    val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
                    if ((medication.weekdays!! and (1 shl today)) == 0) return@filter false
                }

                // Ensure within start/end dates
                if (now < medication.startDate) return@filter false
                if (!medication.isUnlimited && medication.endDate != null && now > medication.endDate) return@filter false

                true
            }
            .map { medication ->
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val time = timeFormat.format(Date(medication.nextDoseTs))
                val title = if (now >= medication.nextDoseTs) {
                    "${medication.name} ${medication.dosage ?: ""} - Take now ($time)"
                } else {
                    "${medication.name} ${medication.dosage ?: ""} - Next dose at $time"
                }

                val intent = Intent(context, DialogLauncherActivity::class.java).apply {
                    putExtra(DialogLauncherActivity.EXTRA_FRAGMENT_CLASS, RecordDoseFragment::class.java.name)
                    putExtra("medicationId", medication.id)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                TargetTemplate.Basic(
                    id = "medication_${medication.id}",
                    componentName = ComponentName(context, MedicationProvider::class.java),
                    featureType = SmartspaceTarget.FEATURE_REMINDER,
                    title = Text(title),
                    subtitle = Text(""),
                    icon = SmartspaceIcon(AndroidIcon.createWithResource(context, R.mipmap.ic_launcher), shouldTint = false),
                    onClick = TapAction(intent = intent)
                ).create()
            }
    }

    override fun getConfig(smartspacerId: String?): Config {
        return Config(
            label = "Medication Reminder",
            description = "A medication reminder",
            icon = AndroidIcon.createWithResource(context, R.mipmap.ic_launcher),
            configActivity = Intent(context, SettingsActivity::class.java)
        )
    }

    override fun onDismiss(smartspacerId: String, targetId: String): Boolean {
        // This will be handled by the RecordDoseActivity
        return false
    }

}

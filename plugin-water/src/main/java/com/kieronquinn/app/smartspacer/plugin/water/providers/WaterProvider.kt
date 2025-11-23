package com.kieronquinn.app.smartspacer.plugin.water.providers

import android.content.ComponentName
import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.water.R
import com.kieronquinn.app.smartspacer.plugin.water.repositories.DisplayMode
import com.kieronquinn.app.smartspacer.plugin.water.repositories.WaterDataRepository
import com.kieronquinn.app.smartspacer.plugin.water.ui.activities.RecordDrinkActivity
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDate
import android.graphics.drawable.Icon as AndroidIcon

class WaterProvider : SmartspacerTargetProvider(), KoinComponent {

    private val waterDataRepository by inject<WaterDataRepository>()

    override fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        val context = this.context ?: return emptyList()
        val today = LocalDate.now()
        val schedule = waterDataRepository.getDailySchedule(today) ?: return emptyList()

        val text = when (waterDataRepository.displayMode) {
            DisplayMode.PROGRESS -> "Water: ${schedule.fulfilledCount} / ${schedule.cupsTotal} cups"
            DisplayMode.REMINDER -> "Time for a glass of water!"
            DisplayMode.DYNAMIC -> {
                val nextReminder = schedule.scheduledTimes.getOrNull(schedule.fulfilledCount)
                if (nextReminder != null && nextReminder <= System.currentTimeMillis()) {
                    "Time for a glass of water!"
                } else {
                    "Water: ${schedule.fulfilledCount} / ${schedule.cupsTotal} cups"
                }
            }
        }

        val intent = Intent(context, RecordDrinkActivity::class.java).apply {
            putExtra("amount", waterDataRepository.cupMl)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val target = TargetTemplate.Basic(
            id = "water_progress",
            componentName = ComponentName(context, this::class.java),
            featureType = SmartspaceTarget.FEATURE_UNDEFINED,
            title = Text(text),
            subtitle = Text(""),
            icon = com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon(AndroidIcon.createWithResource(context, R.drawable.ic_launcher_foreground)),
            onClick = TapAction(intent = intent)
        ).create()

        return listOf(target)
    }

    override fun getConfig(smartspacerId: String?): Config {
        return Config(
            label = "Water Reminder",
            description = "Track your water intake",
            icon = android.graphics.drawable.Icon.createWithResource(context, R.drawable.ic_launcher_foreground),
            configActivity = Intent(context, com.kieronquinn.app.smartspacer.plugin.water.ui.activities.SettingsActivity::class.java)
        )
    }

    override fun onDismiss(smartspacerId: String, targetId: String): Boolean {
        return false
    }
}
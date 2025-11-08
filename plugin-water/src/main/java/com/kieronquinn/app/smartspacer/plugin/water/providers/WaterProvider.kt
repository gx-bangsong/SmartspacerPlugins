package com.kieronquinn.app.smartspacer.plugin.water.providers

import android.content.ComponentName
import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.water.R
import com.kieronquinn.app.smartspacer.plugin.water.repositories.DisplayMode
import com.kieronquinn.app.smartspacer.plugin.water.repositories.WaterDataRepository
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceAction
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDate

class WaterProvider : SmartspacerTargetProvider(), KoinComponent {

    private val waterDataRepository by inject<WaterDataRepository>()

    override fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
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

        val componentName = ComponentName(context!!, this::class.java)
        val target = SmartspaceTarget(
            smartspaceTargetId = "water_progress",
            componentName = componentName,
            featureType = SmartspaceTarget.FEATURE_UNDEFINED,
            headerAction = SmartspaceAction.Builder("water_progress_header", text)
                .setIcon(android.graphics.drawable.Icon.createWithResource(context, R.drawable.ic_launcher_foreground))
                .build()
        )

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
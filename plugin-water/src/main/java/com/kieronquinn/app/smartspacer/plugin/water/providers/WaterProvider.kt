package com.kieronquinn.app.smartspacer.plugin.water.providers

import com.kieronquinn.app.smartspacer.sdk.model.SmartspacerTarget
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.plugin.water.R
import com.kieronquinn.app.smartspacer.plugin.water.repositories.DisplayMode
import com.kieronquinn.app.smartspacer.plugin.water.repositories.WaterDataRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDate

class WaterProvider : SmartspacerTargetProvider(), KoinComponent {

    private val waterDataRepository by inject<WaterDataRepository>()

    override fun getTargets(smartspacerId: String): List<SmartspacerTarget> {
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

        val target = SmartspacerTarget.UI(
            id = "water_progress",
            componentName = "Water Progress",
            icon = Icon(R.drawable.ic_launcher_foreground), // Replace with a proper icon
            primaryText = Text(text),
            tapAction = TapAction(
                intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            )
        )
        return listOf(target)
    }
}
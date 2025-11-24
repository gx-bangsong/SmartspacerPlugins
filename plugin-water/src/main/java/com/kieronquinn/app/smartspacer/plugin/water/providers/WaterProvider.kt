package com.kieronquinn.app.smartspacer.plugin.water.providers

import android.content.ComponentName
import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.water.R
import com.kieronquinn.app.smartspacer.plugin.water.repositories.DisplayMode
import com.kieronquinn.app.smartspacer.plugin.water.repositories.WaterDataRepository
import com.kieronquinn.app.smartspacer.plugin.water.ui.fragments.RecordDrinkFragment
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.DialogLauncherActivity
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.ceil
import android.graphics.drawable.Icon as AndroidIcon

class WaterProvider : SmartspacerTargetProvider(), KoinComponent {

    private val waterDataRepository by inject<WaterDataRepository>()

    override fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        val context = this.context ?: return emptyList()
        val today = LocalDate.now()
        val drinks = runBlocking { waterDataRepository.getDrinksForDate(today) }
        val fulfilledCount = drinks.size
        val cupsTotal = ceil(waterDataRepository.dailyGoalMl.toDouble() / waterDataRepository.cupMl).toInt()

        val activeStart = LocalTime.ofSecondOfDay(waterDataRepository.activeStartMinutes * 60L)
        val activeEnd = LocalTime.ofSecondOfDay(waterDataRepository.activeEndMinutes * 60L)
        val now = LocalTime.now()

        val text = if (now.isBefore(activeStart) || now.isAfter(activeEnd)) {
            // Outside active hours, show progress
            "Water: $fulfilledCount / $cupsTotal cups"
        } else {
            // Inside active hours, calculate next reminder
            val activeDuration = (waterDataRepository.activeEndMinutes - waterDataRepository.activeStartMinutes).toLong()
            val interval = activeDuration / cupsTotal
            val nextDrinkTime = activeStart.plusMinutes(interval * (fulfilledCount + 1))

            if (now.isAfter(nextDrinkTime)) {
                "Time for a glass of water!"
            } else {
                "Water: $fulfilledCount / $cupsTotal cups"
            }
        }

        val intent = Intent(context, DialogLauncherActivity::class.java).apply {
            putExtra(DialogLauncherActivity.EXTRA_FRAGMENT_CLASS, RecordDrinkFragment::class.java.name)
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

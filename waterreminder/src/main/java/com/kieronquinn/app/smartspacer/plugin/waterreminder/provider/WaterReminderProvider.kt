package com.kieronquinn.app.smartspacer.plugin.waterreminder.provider

import android.app.PendingIntent
import android.content.Intent
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerProvider
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceAction
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.BaseTemplateData
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.plugin.waterreminder.data.WaterReminderSettings
import com.kieronquinn.app.smartspacer.plugin.waterreminder.receiver.WaterActionReceiver
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WaterReminderProvider : com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerProvider() {

    private val settings: WaterReminderSettings by inject()

    override suspend fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        val dailyGoal = settings.dailyGoal
        val cupSize = settings.cupSize
        val currentIntake = settings.currentIntake
        val cupsDrunk = currentIntake / cupSize
        val totalCups = dailyGoal / cupSize

        val addDrinkIntent = Intent(context, WaterActionReceiver::class.java).apply {
            action = WaterActionReceiver.ACTION_DRINK_WATER
        }
        val addDrinkPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            addDrinkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextReminderTime = getNextReminderTime()
        val nextReminderTimeString = if (nextReminderTime != null) {
            "Next at ${java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(java.util.Date(nextReminderTime))}"
        } else {
            ""
        }

        val title = when (settings.uiStyle) {
            1 -> "Water: $cupsDrunk / $totalCups cups"
            2 -> "Time for a glass of water!"
            else -> "Water: $cupsDrunk / $totalCups. $nextReminderTimeString"
        }

        return listOf(
            SmartspaceTarget(
                smartspaceTargetId = "water_reminder",
                featureType = SmartspaceTarget.FEATURE_BASIC,
                componentName = componentName,
                templateData = BaseTemplateData(
                    primaryText = Text(title)
                ),
                tapAction = SmartspaceAction(
                    id = "drink_water",
                    intent = addDrinkIntent,
                    pendingIntent = addDrinkPendingIntent,
                    title = "Drink Water"
                )
            )
        )
    }

    private fun getNextReminderTime(): Long? {
        if (!settings.remindersEnabled) {
            return null
        }

        val now = java.util.Calendar.getInstance()
        val nowInMillis = now.timeInMillis
        val startHour = settings.activeHoursStart / 60
        val startMinute = settings.activeHoursStart % 60
        val endHour = settings.activeHoursEnd / 60
        val endMinute = settings.activeHoursEnd % 60

        val startCalendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, startHour)
            set(java.util.Calendar.MINUTE, startMinute)
        }
        val endCalendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, endHour)
            set(java.util.Calendar.MINUTE, endMinute)
        }

        if (nowInMillis > endCalendar.timeInMillis) {
            return null
        }

        if (nowInMillis < startCalendar.timeInMillis) {
            return startCalendar.timeInMillis
        }

        val totalCups = settings.dailyGoal / settings.cupSize
        if (totalCups == 0) return null
        val activeDuration = (settings.activeHoursEnd - settings.activeHoursStart).toLong()
        val interval = activeDuration / totalCups

        var nextReminderTime = startCalendar.timeInMillis
        while (nextReminderTime < endCalendar.timeInMillis) {
            if (nextReminderTime > nowInMillis) {
                return nextReminderTime
            }
            nextReminderTime += interval * 60 * 1000
        }

        return null
    }
}
package com.kieronquinn.app.smartspacer.plugin.waterreminder.providers

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceAction
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.plugin.waterreminder.R
import com.kieronquinn.app.smartspacer.plugin.waterreminder.data.WaterReminderSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.waterreminder.receivers.WaterActionReceiver
import com.kieronquinn.app.smartspacer.plugin.waterreminder.ui.settings.WaterReminderSettingsActivity
import org.koin.core.component.inject
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class WaterReminderProvider : SmartspacerTargetProvider() {

    override fun getConfig(smartspacerId: String?): Config {
        return Config(
            label = "Water Reminder",
            description = "Reminds you to drink water",
            icon = R.drawable.ic_launcher_foreground
        )
    }

    companion object {
        private const val TAG = "WaterReminderProvider"
        const val ACTION_REFRESH = "com.kieronquinn.app.smartspacer.plugin.waterreminder.REFRESH"
    }

    private val settingsRepository: WaterReminderSettingsRepository by inject()

    override suspend fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        val goalMl = settingsRepository.dailyGoalMl.get()
        val cupSizeMl = settingsRepository.cupSizeMl.get()

        if (goalMl <= 0 || cupSizeMl <= 0) {
            return emptyList()
        }

        val goalCups = (goalMl + cupSizeMl - 1) / cupSizeMl // Ceiling division
        val progressCups = settingsRepository.currentProgressCups.get()
        val spacerStyle = settingsRepository.spacerStyle.get()

        val nextReminderTime = calculateNextReminderTime()
        val timeFormatted = nextReminderTime?.format(DateTimeFormatter.ofPattern("h:mm a")) ?: ""

        val title = when (spacerStyle) {
            1 -> "Next glass at $timeFormatted"
            2 -> "Water: $progressCups/$goalCups cups. Next at $timeFormatted"
            else -> "Water: $progressCups / $goalCups cups"
        }

        val logWaterIntent = Intent(context, WaterActionReceiver::class.java).apply {
            action = WaterActionReceiver.ACTION_LOG_WATER
        }
        val logWaterPendingIntent = PendingIntent.getBroadcast(
            context, 0, logWaterIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val settingsIntent = Intent(context, WaterReminderSettingsActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val target = SmartspaceTarget(
            id = "water_reminder_target",
            componentName = componentName,
            smartspaceTargetId = "water_reminder_target",
            headerAction = SmartspaceAction(
                id = "water_reminder_header",
                title = title,
                pendingIntent = logWaterPendingIntent,
                icon = Icon(R.drawable.ic_water_drop)
            ),
        )

        Log.d(TAG, "Providing water reminder target: $title")
        return listOf(target)
    }

    private suspend fun calculateNextReminderTime(): LocalTime? {
        val startHour = settingsRepository.activeHourStart.get()
        val endHour = settingsRepository.activeHourEnd.get()
        val goalCups = (settingsRepository.dailyGoalMl.get() + settingsRepository.cupSizeMl.get() - 1) / settingsRepository.cupSizeMl.get()
        val progressCups = settingsRepository.currentProgressCups.get()
        val now = LocalTime.now()

        if (progressCups >= goalCups || now.hour > endHour) {
            return null // Goal met or past active hours
        }

        val remainingCups = goalCups - progressCups
        if (remainingCups <= 0) return null

        val effectiveStartHour = if (now.hour < startHour) startHour else now.hour
        val remainingHours = (endHour - effectiveStartHour).coerceAtLeast(1)
        val intervalMinutes = (remainingHours * 60) / remainingCups

        return now.plusMinutes(intervalMinutes.toLong())
    }

    override fun onReceive(intent: Intent) {
        super.onReceive(intent)
        if (intent.action == ACTION_REFRESH) {
            Log.d(TAG, "Received refresh broadcast, notifying change.")
            notifyChange()
        }
    }
}
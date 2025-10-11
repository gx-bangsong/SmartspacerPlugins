package com.kieronquinn.app.smartspacer.plugin.waterreminder.data

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.BaseSettingsRepositoryImpl
import kotlinx.coroutines.flow.MutableSharedFlow

class WaterReminderSettingsRepository(
    context: Context,
    val onChange: MutableSharedFlow<String> = MutableSharedFlow(replay = 1)
) : BaseSettingsRepositoryImpl() {

    override suspend fun getBackup(): Map<String, String> {
        return emptyMap()
    }

    override suspend fun restoreBackup(settings: Map<String, String>) {
        // No-op
    }

    companion object {
        private const val PREFERENCES_NAME = "water_reminder_settings"
        private const val KEY_DAILY_GOAL_ML = "daily_goal_ml"
        private const val KEY_CUP_SIZE_ML = "cup_size_ml"
        private const val KEY_CURRENT_PROGRESS_CUPS = "current_progress_cups"
        private const val KEY_ACTIVE_HOUR_START = "active_hour_start"
        private const val KEY_ACTIVE_HOUR_END = "active_hour_end"
        private const val KEY_SPACER_STYLE = "spacer_style"
        private const val KEY_LAST_RESET_TIMESTAMP = "last_reset_timestamp"
    }

    override val sharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    val dailyGoalMl = int(KEY_DAILY_GOAL_ML, 2000, onChange)
    val cupSizeMl = int(KEY_CUP_SIZE_ML, 250, onChange)
    val currentProgressCups = int(KEY_CURRENT_PROGRESS_CUPS, 0, onChange)
    val activeHourStart = int(KEY_ACTIVE_HOUR_START, 8, onChange) // 8 AM
    val activeHourEnd = int(KEY_ACTIVE_HOUR_END, 22, onChange) // 10 PM
    val spacerStyle = int(KEY_SPACER_STYLE, 0, onChange) // 0: Progress, 1: Next reminder, 2: Combined
    val lastResetTimestamp = long(KEY_LAST_RESET_TIMESTAMP, 0L, onChange)
}
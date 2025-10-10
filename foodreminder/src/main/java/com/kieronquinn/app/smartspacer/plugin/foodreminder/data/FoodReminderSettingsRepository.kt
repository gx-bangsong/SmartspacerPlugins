package com.kieronquinn.app.smartspacer.plugin.foodreminder.data

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.BaseSettingsRepositoryImpl
import kotlinx.coroutines.flow.MutableSharedFlow

class FoodReminderSettingsRepository(
    context: Context,
    val onChange: MutableSharedFlow<String> = MutableSharedFlow(replay = 1)
) : BaseSettingsRepositoryImpl() {

    companion object {
        private const val PREFERENCES_NAME = "food_reminder_settings"
        private const val KEY_REMINDER_LEAD_TIME_DAYS = "reminder_lead_time_days"
    }

    override val sharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    /**
     * How many days before expiry to show a reminder.
     * e.g., 0 = on the day, 1 = 1 day before, etc.
     */
    val reminderLeadTimeDays = int(KEY_REMINDER_LEAD_TIME_DAYS, 1, onChange)
}
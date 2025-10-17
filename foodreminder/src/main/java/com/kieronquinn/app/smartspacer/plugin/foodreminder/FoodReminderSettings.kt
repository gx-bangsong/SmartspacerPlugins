package com.kieronquinn.app.smartspacer.plugin.foodreminder

import android.content.Context
import android.content.SharedPreferences

class FoodReminderSettings(context: Context) {
    companion object {
        private const val PREFS_NAME = "food_reminder_prefs"
        private const val KEY_REMINDER_TIMEFRAME = "reminder_timeframe"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var reminderTimeframe: Int
        get() = prefs.getInt(KEY_REMINDER_TIMEFRAME, 1) // Default to 1 day
        set(value) = prefs.edit().putInt(KEY_REMINDER_TIMEFRAME, value).apply()
}
package com.kieronquinn.app.smartspacer.plugin.medicationreminder

import android.content.Context
import android.content.SharedPreferences

class MedicationReminderSettings(context: Context) {
    companion object {
        private const val PREFS_NAME = "medication_reminder_prefs"
        private const val KEY_SNOOZE_DURATION = "snooze_duration"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var snoozeDuration: Int
        get() = prefs.getInt(KEY_SNOOZE_DURATION, 15) // Default to 15 minutes
        set(value) = prefs.edit().putInt(KEY_SNOOZE_DURATION, value).apply()
}
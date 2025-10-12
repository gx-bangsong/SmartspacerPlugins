package com.kieronquinn.app.smartspacer.plugin.waterreminder.data

import android.content.Context
import android.content.SharedPreferences

class WaterReminderSettings(context: Context) {

    companion object {
        private const val PREFS_NAME = "water_reminder_prefs"
        private const val KEY_DAILY_GOAL = "daily_goal"
        private const val KEY_CUP_SIZE = "cup_size"
        private const val KEY_REMINDERS_ENABLED = "reminders_enabled"
        private const val KEY_ACTIVE_HOURS_START = "active_hours_start"
        private const val KEY_ACTIVE_HOURS_END = "active_hours_end"
        private const val KEY_CURRENT_INTAKE = "current_intake"
        private const val KEY_UI_STYLE = "ui_style"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var dailyGoal: Int
        get() = prefs.getInt(KEY_DAILY_GOAL, 2000)
        set(value) = prefs.edit().putInt(KEY_DAILY_GOAL, value).apply()

    var cupSize: Int
        get() = prefs.getInt(KEY_CUP_SIZE, 250)
        set(value) = prefs.edit().putInt(KEY_CUP_SIZE, value).apply()

    var remindersEnabled: Boolean
        get() = prefs.getBoolean(KEY_REMINDERS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_REMINDERS_ENABLED, value).apply()

    var activeHoursStart: Int
        get() = prefs.getInt(KEY_ACTIVE_HOURS_START, 8 * 60) // 8:00 AM in minutes
        set(value) = prefs.edit().putInt(KEY_ACTIVE_HOURS_START, value).apply()

    var activeHoursEnd: Int
        get() = prefs.getInt(KEY_ACTIVE_HOURS_END, 22 * 60) // 10:00 PM in minutes
        set(value) = prefs.edit().putInt(KEY_ACTIVE_HOURS_END, value).apply()

    var currentIntake: Int
        get() = prefs.getInt(KEY_CURRENT_INTAKE, 0)
        set(value) = prefs.edit().putInt(KEY_CURRENT_INTAKE, value).apply()

    var uiStyle: Int
        get() = prefs.getInt(KEY_UI_STYLE, 0)
        set(value) = prefs.edit().putInt(KEY_UI_STYLE, value).apply()
}
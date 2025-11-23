package com.kieronquinn.app.smartspacer.plugin.water.repositories

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.kieronquinn.app.smartspacer.plugin.water.data.DrinkHistory
import com.kieronquinn.app.smartspacer.plugin.water.data.DrinkHistoryDao
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

interface WaterDataRepository {
    var dailyGoalMl: Int
    var cupMl: Int
    var activeStartMinutes: Int
    var activeEndMinutes: Int
    var displayMode: DisplayMode
    var resetAtActiveStart: Boolean
    var smartAdjust: Boolean
    var snoozeMinutes: Int

    suspend fun getDrinksForDate(date: LocalDate): List<DrinkHistory>
}

enum class DisplayMode {
    PROGRESS, REMINDER, DYNAMIC
}

class WaterDataRepositoryImpl(
    context: Context,
    private val drinkHistoryDao: DrinkHistoryDao
) : WaterDataRepository {

    companion object {
        private const val PREFS_NAME = "plugin_water_prefs"

        private const val KEY_GOAL_ML = "WATER_GOAL_ML"
        private const val KEY_CUP_ML = "WATER_CUP_ML"
        private const val KEY_ACTIVE_START_MIN = "WATER_ACTIVE_START_MIN"
        private const val KEY_ACTIVE_END_MIN = "WATER_ACTIVE_END_MIN"
        private const val KEY_DISPLAY_MODE = "WATER_DISPLAY_MODE"
        private const val KEY_RESET_AT_ACTIVE = "WATER_RESET_AT_ACTIVE"
        private const val KEY_SMART_ADJUST = "WATER_SMART_ADJUST"
        private const val KEY_SNOOZE_MIN = "WATER_SNOOZE_MIN"

        private const val DEFAULT_GOAL_ML = 2000
        private const val DEFAULT_CUP_ML = 250
        private const val DEFAULT_ACTIVE_START_MIN = 8 * 60 // 08:00
        private const val DEFAULT_ACTIVE_END_MIN = 22 * 60 // 22:00
        private const val DEFAULT_DISPLAY_MODE = "DYNAMIC"
        private const val DEFAULT_RESET_AT_ACTIVE = true
        private const val DEFAULT_SMART_ADJUST = true
        private const val DEFAULT_SNOOZE_MIN = 10
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override var dailyGoalMl: Int
        get() = prefs.getInt(KEY_GOAL_ML, DEFAULT_GOAL_ML)
        set(value) = prefs.edit().putInt(KEY_GOAL_ML, value).apply()

    override var cupMl: Int
        get() = prefs.getInt(KEY_CUP_ML, DEFAULT_CUP_ML)
        set(value) = prefs.edit().putInt(KEY_CUP_ML, value).apply()

    override var activeStartMinutes: Int
        get() = prefs.getInt(KEY_ACTIVE_START_MIN, DEFAULT_ACTIVE_START_MIN)
        set(value) = prefs.edit().putInt(KEY_ACTIVE_START_MIN, value).apply()

    override var activeEndMinutes: Int
        get() = prefs.getInt(KEY_ACTIVE_END_MIN, DEFAULT_ACTIVE_END_MIN)
        set(value) = prefs.edit().putInt(KEY_ACTIVE_END_MIN, value).apply()

    override var displayMode: DisplayMode
        get() = DisplayMode.valueOf(prefs.getString(KEY_DISPLAY_MODE, DEFAULT_DISPLAY_MODE) ?: DEFAULT_DISPLAY_MODE)
        set(value) = prefs.edit().putString(KEY_DISPLAY_MODE, value.name).apply()

    override var resetAtActiveStart: Boolean
        get() = prefs.getBoolean(KEY_RESET_AT_ACTIVE, DEFAULT_RESET_AT_ACTIVE)
        set(value) = prefs.edit().putBoolean(KEY_RESET_AT_ACTIVE, value).apply()

    override var smartAdjust: Boolean
        get() = prefs.getBoolean(KEY_SMART_ADJUST, DEFAULT_SMART_ADJUST)
        set(value) = prefs.edit().putBoolean(KEY_SMART_ADJUST, value).apply()

    override var snoozeMinutes: Int
        get() = prefs.getInt(KEY_SNOOZE_MIN, DEFAULT_SNOOZE_MIN)
        set(value) = prefs.edit().putInt(KEY_SNOOZE_MIN, value).apply()

    override suspend fun getDrinksForDate(date: LocalDate): List<DrinkHistory> {
        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return drinkHistoryDao.getDrinksForDate(startOfDay, endOfDay)
    }
}

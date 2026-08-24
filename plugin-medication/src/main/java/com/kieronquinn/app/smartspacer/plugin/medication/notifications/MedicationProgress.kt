package com.kieronquinn.app.smartspacer.plugin.medication.notifications

import com.google.gson.Gson
import com.kieronquinn.app.smartspacer.plugin.medication.data.Medication
import com.kieronquinn.app.smartspacer.plugin.medication.data.ScheduleType
import java.util.Calendar

object MedicationProgress {

    private val gson = Gson()

    fun scheduledToday(medication: Medication, now: Calendar = Calendar.getInstance()): Int {
        return when (medication.scheduleType) {
            ScheduleType.SPECIFIC_TIMES -> {
                parseTimes(medication.timesOfDay).size
            }
            ScheduleType.EVERY_X_HOURS -> {
                val interval = (medication.intervalHours ?: 1).coerceAtLeast(1)
                val first = parseSingleTime(medication.timesOfDay) ?: return 1
                var count = 0
                var minutes = first
                while (minutes < 24 * 60) {
                    count++
                    minutes += interval * 60
                }
                count.coerceAtLeast(1)
            }
            ScheduleType.EVERY_X_DAYS,
            ScheduleType.SPECIFIC_WEEKDAYS -> 1
        }
    }

    fun isMultiDoseToday(medication: Medication, now: Calendar = Calendar.getInstance()): Boolean {
        return scheduledToday(medication, now) >= 2
    }

    fun capsule(taken: Int, scheduled: Int): String = "$taken/$scheduled"

    private fun parseTimes(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            gson.fromJson(raw, Array<String>::class.java)?.toList().orEmpty()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseSingleTime(raw: String?): Int? {
        val parts = raw?.split(":") ?: return null
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: return null
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return hour * 60 + minute
    }
}

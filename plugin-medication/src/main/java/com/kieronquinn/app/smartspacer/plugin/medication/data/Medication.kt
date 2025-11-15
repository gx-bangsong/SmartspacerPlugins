package com.kieronquinn.app.smartspacer.plugin.medication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val dosage: String?,
    val startDate: Long,
    val endDate: Long?,
    val isUnlimited: Boolean,
    val scheduleType: ScheduleType,
    val intervalHours: Int?,
    val intervalDays: Int?,
    val timesOfDay: String?, // JSON list of times, e.g., ["08:00", "20:00"]
    val weekdays: Int?, // Bitmask for weekdays
    val nextDoseTs: Long,
    val enabled: Boolean = true
)

enum class ScheduleType {
    EVERY_X_HOURS,
    EVERY_X_DAYS,
    SPECIFIC_TIMES,
    SPECIFIC_WEEKDAYS
}
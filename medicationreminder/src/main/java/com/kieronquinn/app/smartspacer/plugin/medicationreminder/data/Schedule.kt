package com.kieronquinn.app.smartspacer.plugin.medicationreminder.data

data class Schedule(
    val type: ScheduleType,
    val times: List<String>?, // For specific times
    val interval: Int?, // For every X hours/days
    val daysOfWeek: List<Int>? // For specific days of the week
)

enum class ScheduleType {
    EVERY_X_HOURS,
    SPECIFIC_TIMES,
    EVERY_X_DAYS,
    SPECIFIC_DAYS_OF_WEEK
}
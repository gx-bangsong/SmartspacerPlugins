package com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.model

import java.time.DayOfWeek
import java.time.LocalTime

sealed class Schedule {
    data class EveryXHours(val hours: Int) : Schedule()
    data class SpecificTimes(val times: List<LocalTime>) : Schedule()
    data class EveryXDays(val days: Int, val time: LocalTime) : Schedule()
    data class SpecificDaysOfWeek(val days: Set<DayOfWeek>, val time: LocalTime) : Schedule()
}
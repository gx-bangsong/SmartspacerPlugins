package com.kieronquinn.app.smartspacer.plugin.medicationreminder.logic

import com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.model.DoseLog
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.model.Medication
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.model.Schedule
import java.time.*

object SchedulingEngine {

    fun getNextDueDate(medication: Medication, doseLogs: List<DoseLog>): LocalDateTime? {
        val now = LocalDateTime.now()
        val medicationStart = Instant.ofEpochMilli(medication.startDate).atZone(ZoneId.systemDefault()).toLocalDateTime()

        if (now.isBefore(medicationStart)) return null

        medication.endDate?.let {
            val medicationEnd = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime()
            if (now.isAfter(medicationEnd)) return null
        }

        val lastDoseTime = doseLogs.maxOfOrNull { it.doseTimestamp }?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime()
        }

        val effectiveStartDate = lastDoseTime ?: medicationStart

        return when (val schedule = medication.schedule) {
            is Schedule.EveryXHours -> getNextForEveryXHours(schedule, effectiveStartDate, now)
            is Schedule.SpecificTimes -> getNextForSpecificTimes(schedule, lastDoseTime, now)
            is Schedule.EveryXDays -> getNextForEveryXDays(schedule, effectiveStartDate, now)
            is Schedule.SpecificDaysOfWeek -> getNextForSpecificDaysOfWeek(schedule, lastDoseTime, now)
        }
    }

    private fun getNextForEveryXHours(schedule: Schedule.EveryXHours, lastDose: LocalDateTime, now: LocalDateTime): LocalDateTime {
        var nextDose = lastDose.plusHours(schedule.hours.toLong())
        while (nextDose.plusHours(schedule.hours.toLong()).isBefore(now)) {
            nextDose = nextDose.plusHours(schedule.hours.toLong())
        }
        return nextDose
    }

    private fun getNextForSpecificTimes(schedule: Schedule.SpecificTimes, lastDoseTime: LocalDateTime?, now: LocalDateTime): LocalDateTime? {
        val potentialTimes = schedule.times.flatMap { time ->
            // Check from yesterday up to 2 days in the future to find the next valid time
            listOf(
                now.toLocalDate().minusDays(1).atTime(time),
                now.toLocalDate().atTime(time),
                now.toLocalDate().plusDays(1).atTime(time),
                now.toLocalDate().plusDays(2).atTime(time)
            )
        }.sorted()

        return potentialTimes.find { lastDoseTime == null || it.isAfter(lastDoseTime) }
    }

    private fun getNextForEveryXDays(schedule: Schedule.EveryXDays, lastDose: LocalDateTime, now: LocalDateTime): LocalDateTime {
        var nextDose = lastDose.toLocalDate().atTime(schedule.time)
        if (!nextDose.isAfter(lastDose)) {
             nextDose = nextDose.plusDays(schedule.days.toLong())
        }
        while (nextDose.isBefore(now)) {
            nextDose = nextDose.plusDays(schedule.days.toLong())
        }
        return nextDose
    }

    private fun getNextForSpecificDaysOfWeek(schedule: Schedule.SpecificDaysOfWeek, lastDoseTime: LocalDateTime?, now: LocalDateTime): LocalDateTime? {
        val sortedDays = schedule.days.sorted()
        if (sortedDays.isEmpty()) return null

        var searchDate = (lastDoseTime ?: now.minusDays(1)).toLocalDate()

        for (i in 0..14) {
            val currentDate = searchDate.plusDays(i.toLong())
            if (sortedDays.contains(currentDate.dayOfWeek)) {
                val potentialNextDose = currentDate.atTime(schedule.time)
                if (lastDoseTime == null || potentialNextDose.isAfter(lastDoseTime)) {
                    return potentialNextDose
                }
            }
        }
        return null
    }
}
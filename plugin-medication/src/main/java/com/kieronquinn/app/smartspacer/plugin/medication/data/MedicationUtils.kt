package com.kieronquinn.app.smartspacer.plugin.medication.data

import com.google.gson.Gson
import java.util.*

object MedicationUtils {
    private val gson = Gson()

    fun calculateNextDose(medication: Medication): Long {
        val now = Calendar.getInstance()
        val startCal = Calendar.getInstance().apply { timeInMillis = medication.startDate }

        return when (medication.scheduleType) {
            ScheduleType.SPECIFIC_TIMES -> {
                val times = gson.fromJson(medication.timesOfDay, Array<String>::class.java).toList()
                for (time in times.sorted()) {
                    val (hour, minute) = time.split(":").map { it.toInt() }
                    val doseTime = (now.clone() as Calendar).apply {
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    if (doseTime.after(now) && !doseTime.before(startCal)) {
                        return doseTime.timeInMillis
                    }
                }
                val (hour, minute) = times.sorted().first().split(":").map { it.toInt() }
                (now.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
            ScheduleType.EVERY_X_HOURS -> {
                val (hour, minute) = medication.timesOfDay!!.split(":").map { it.toInt() }
                val firstDose = (startCal.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (firstDose.after(now)) return firstDose.timeInMillis

                val diffMillis = now.timeInMillis - firstDose.timeInMillis
                val intervalMillis = (medication.intervalHours ?: 1) * 60 * 60 * 1000L
                val intervalsPassed = (diffMillis / intervalMillis) + 1
                firstDose.timeInMillis + (intervalsPassed * intervalMillis)
            }
            ScheduleType.EVERY_X_DAYS -> {
                val (hour, minute) = medication.timesOfDay!!.split(":").map { it.toInt() }
                val firstDose = (startCal.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (firstDose.after(now)) return firstDose.timeInMillis

                val diffMillis = now.timeInMillis - firstDose.timeInMillis
                val intervalMillis = (medication.intervalDays ?: 1) * 24 * 60 * 60 * 1000L
                val intervalsPassed = (diffMillis / intervalMillis) + 1
                firstDose.timeInMillis + (intervalsPassed * intervalMillis)
            }
            ScheduleType.SPECIFIC_WEEKDAYS -> {
                val (hour, minute) = medication.timesOfDay!!.split(":").map { it.toInt() }
                val doseTime = (now.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                for (i in 0..7) {
                    val checkTime = (doseTime.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, i) }
                    val dayOfWeek = checkTime.get(Calendar.DAY_OF_WEEK)
                    if ((medication.weekdays!! and (1 shl dayOfWeek)) != 0) {
                        if (checkTime.after(now) && !checkTime.before(startCal)) {
                            return checkTime.timeInMillis
                        }
                    }
                }
                0L
            }
        }
    }
}

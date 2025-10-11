package com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.converters

import androidx.room.TypeConverter
import com.google.gson.GsonBuilder
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.model.Schedule

class ScheduleConverter {
    private val gson = GsonBuilder()
        .registerTypeAdapter(Schedule::class.java, ScheduleTypeAdapter())
        .create()

    @TypeConverter
    fun fromSchedule(schedule: Schedule?): String? {
        return schedule?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toSchedule(scheduleJson: String?): Schedule? {
        return scheduleJson?.let {
            gson.fromJson(it, Schedule::class.java)
        }
    }
}
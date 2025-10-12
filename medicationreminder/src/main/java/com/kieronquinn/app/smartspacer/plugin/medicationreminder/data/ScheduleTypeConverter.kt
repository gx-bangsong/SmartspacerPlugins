package com.kieronquinn.app.smartspacer.plugin.medicationreminder.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ScheduleTypeConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromSchedule(schedule: Schedule): String {
        return gson.toJson(schedule)
    }

    @TypeConverter
    fun toSchedule(scheduleString: String): Schedule {
        val type = object : TypeToken<Schedule>() {}.type
        return gson.fromJson(scheduleString, type)
    }

    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        return list?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toStringList(string: String?): List<String>? {
        return string?.let { gson.fromJson(it, object : TypeToken<List<String>>() {}.type) }
    }

    @TypeConverter
    fun fromIntList(list: List<Int>?): String? {
        return list?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toIntList(string: String?): List<Int>? {
        return string?.let { gson.fromJson(it, object : TypeToken<List<Int>>() {}.type) }
    }
}
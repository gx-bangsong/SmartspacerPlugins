package com.kieronquinn.app.smartspacer.plugin.medication.data

import androidx.room.TypeConverter

class TypeConverters {

    @TypeConverter
    fun toScheduleType(value: String) = enumValueOf<ScheduleType>(value)

    @TypeConverter
    fun fromScheduleType(value: ScheduleType) = value.name

}

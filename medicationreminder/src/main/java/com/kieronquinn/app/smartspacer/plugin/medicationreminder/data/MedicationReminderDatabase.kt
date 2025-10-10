package com.kieronquinn.app.smartspacer.plugin.medicationreminder.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.converters.ScheduleConverter
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.model.DoseLog
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.model.Medication

@Database(entities = [Medication::class, DoseLog::class], version = 1, exportSchema = false)
@TypeConverters(ScheduleConverter::class)
abstract class MedicationReminderDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
}
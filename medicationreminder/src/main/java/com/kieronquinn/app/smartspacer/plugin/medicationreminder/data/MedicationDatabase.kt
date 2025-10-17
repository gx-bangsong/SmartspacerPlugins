package com.kieronquinn.app.smartspacer.plugin.medicationreminder.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Medication::class, TakenDose::class], version = 2)
@TypeConverters(ScheduleTypeConverter::class)
abstract class MedicationDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun takenDoseDao(): TakenDoseDao
}
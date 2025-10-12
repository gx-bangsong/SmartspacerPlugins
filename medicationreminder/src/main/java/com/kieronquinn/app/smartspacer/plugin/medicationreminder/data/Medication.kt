package com.kieronquinn.app.smartspacer.plugin.medicationreminder.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val dosage: String?,
    val startDate: Long,
    val endDate: Long?,
    @Embedded val schedule: Schedule
)
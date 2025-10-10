package com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val dosage: String?,
    val startDate: Long,
    val endDate: Long?, // Null for unlimited
    val schedule: Schedule
)
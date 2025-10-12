package com.kieronquinn.app.smartspacer.plugin.medicationreminder.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "taken_doses")
data class TakenDose(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val medicationId: Int,
    val doseTime: Long,
    val takenTime: Long
)
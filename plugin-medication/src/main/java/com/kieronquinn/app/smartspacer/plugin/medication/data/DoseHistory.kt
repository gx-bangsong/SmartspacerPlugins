package com.kieronquinn.app.smartspacer.plugin.medication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dose_history")
data class DoseHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val medicationId: Int,
    val timestamp: Long,
    val status: Status
) {
    enum class Status {
        TAKEN,
        SKIPPED
    }
}

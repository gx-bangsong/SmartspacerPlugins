package com.kieronquinn.app.smartspacer.plugin.checkin.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "check_in_items")
data class CheckInItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: String, // format "yyyy-MM-dd"
    val checkInTime: Long?, // Epoch millis
    val checkOutTime: Long?, // Epoch millis
    val timestamp: Long = System.currentTimeMillis()
)

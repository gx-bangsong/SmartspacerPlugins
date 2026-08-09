package com.kieronquinn.app.smartspacer.plugin.travel.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "travel_info_items")
data class TravelInfoItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val trainNumber: String,
    val departureStation: String,
    val arrivalStation: String?,
    val departureTime: Long, // Epoch millis
    val seat: String?,
    val passengerName: String?,
    val source: String, // "sms" or "manual"
    val timestamp: Long = System.currentTimeMillis(),
    val isUsed: Boolean = false
)

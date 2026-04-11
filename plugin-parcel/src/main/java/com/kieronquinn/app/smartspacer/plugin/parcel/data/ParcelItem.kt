package com.kieronquinn.app.smartspacer.plugin.parcel.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parcels")
data class ParcelItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pickupCode: String,
    val stationName: String?,
    val rawText: String,
    val timestamp: Long,
    val status: Status = Status.PENDING
) {
    enum class Status {
        PENDING,
        PICKED_UP,
        EXPIRED
    }
}

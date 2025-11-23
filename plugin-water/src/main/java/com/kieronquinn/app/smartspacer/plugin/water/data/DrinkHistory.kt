package com.kieronquinn.app.smartspacer.plugin.water.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drink_history")
data class DrinkHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val amount: Int
)

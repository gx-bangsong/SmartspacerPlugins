package com.kieronquinn.app.smartspacer.plugin.food.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_items")
data class FoodItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val storageMethod: String,
    val expiryDate: Long,
    val addedAt: Long = System.currentTimeMillis(),
    val reminderOffsetDays: Int,
    val notes: String?,
    val enabled: Boolean = true
)
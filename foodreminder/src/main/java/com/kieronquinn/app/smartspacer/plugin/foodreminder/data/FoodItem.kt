package com.kieronquinn.app.smartspacer.plugin.foodreminder.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_items")
data class FoodItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val storageMethod: String,
    val expiryDate: Long
)
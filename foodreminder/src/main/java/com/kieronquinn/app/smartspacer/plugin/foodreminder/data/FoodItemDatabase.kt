package com.kieronquinn.app.smartspacer.plugin.foodreminder.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FoodItem::class], version = 1)
abstract class FoodItemDatabase : RoomDatabase() {
    abstract fun foodItemDao(): FoodItemDao
}
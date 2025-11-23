package com.kieronquinn.app.smartspacer.plugin.water.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DrinkHistory::class], version = 1, exportSchema = false)
abstract class WaterDatabase : RoomDatabase() {

    abstract fun drinkHistoryDao(): DrinkHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: WaterDatabase? = null

        fun getDatabase(context: Context): WaterDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WaterDatabase::class.java,
                    "water_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

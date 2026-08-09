package com.kieronquinn.app.smartspacer.plugin.travel.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TravelInfoItem::class], version = 1, exportSchema = false)
abstract class TravelInfoDatabase : RoomDatabase() {

    abstract fun travelInfoDao(): TravelInfoDao

    companion object {
        @Volatile
        private var INSTANCE: TravelInfoDatabase? = null

        fun getDatabase(context: Context): TravelInfoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TravelInfoDatabase::class.java,
                    "travel_info_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

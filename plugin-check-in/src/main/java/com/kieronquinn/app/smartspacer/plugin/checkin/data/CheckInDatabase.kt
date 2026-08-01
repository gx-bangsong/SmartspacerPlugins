package com.kieronquinn.app.smartspacer.plugin.checkin.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CheckInItem::class], version = 1, exportSchema = false)
abstract class CheckInDatabase : RoomDatabase() {

    abstract fun checkInDao(): CheckInDao

    companion object {
        @Volatile
        private var INSTANCE: CheckInDatabase? = null

        fun getDatabase(context: Context): CheckInDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CheckInDatabase::class.java,
                    "check_in_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

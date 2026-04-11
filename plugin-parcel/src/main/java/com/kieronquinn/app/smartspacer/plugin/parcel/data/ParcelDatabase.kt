package com.kieronquinn.app.smartspacer.plugin.parcel.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

@Database(entities = [ParcelItem::class, RuleItem::class], version = 2, exportSchema = false)
@TypeConverters(ParcelDatabase.Converters::class)
abstract class ParcelDatabase : RoomDatabase() {
    abstract fun parcelDao(): ParcelDao
    abstract fun ruleDao(): RuleDao

    companion object {
        @Volatile
        private var INSTANCE: ParcelDatabase? = null

        fun getInstance(context: Context): ParcelDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ParcelDatabase::class.java,
                    "parcel_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }

    class Converters {
        @TypeConverter
        fun fromStatus(status: ParcelItem.Status): String = status.name

        @TypeConverter
        fun toStatus(value: String): ParcelItem.Status = ParcelItem.Status.valueOf(value)
    }
}

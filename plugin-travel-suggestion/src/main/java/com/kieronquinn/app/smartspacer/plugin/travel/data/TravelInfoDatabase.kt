package com.kieronquinn.app.smartspacer.plugin.travel.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [TravelInfoItem::class], version = 2, exportSchema = false)
abstract class TravelInfoDatabase : RoomDatabase() {

    abstract fun travelInfoDao(): TravelInfoDao

    companion object {
        @Volatile
        private var INSTANCE: TravelInfoDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE travel_info_items ADD COLUMN gate TEXT")
            }
        }

        fun getDatabase(context: Context): TravelInfoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TravelInfoDatabase::class.java,
                    "travel_info_database"
                ).addMigrations(MIGRATION_1_2).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

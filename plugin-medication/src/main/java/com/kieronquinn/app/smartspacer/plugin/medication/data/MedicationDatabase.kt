package com.kieronquinn.app.smartspacer.plugin.medication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Medication::class, DoseHistory::class], version = 2, exportSchema = false)
@TypeConverters(com.kieronquinn.app.smartspacer.plugin.medication.data.TypeConverters::class)
abstract class MedicationDatabase : RoomDatabase() {

    abstract fun medicationDao(): MedicationDao
    abstract fun doseHistoryDao(): DoseHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: MedicationDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `dose_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `medicationId` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, `status` TEXT NOT NULL)")
            }
        }

        fun getDatabase(context: Context): MedicationDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MedicationDatabase::class.java,
                    "medication_database"
                ).addMigrations(MIGRATION_1_2).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

package com.kieronquinn.app.smartspacer.plugin.medicationreminder

import androidx.room.Room
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.MedicationDatabase
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.MedicationRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val medicationReminderModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            MedicationDatabase::class.java, "medication_database"
        ).fallbackToDestructiveMigration().build()
    }
    single { get<MedicationDatabase>().medicationDao() }
    single { get<MedicationDatabase>().takenDoseDao() }
    single { MedicationRepository(get(), get()) }
    single { MedicationReminderSettings(androidContext()) }
}
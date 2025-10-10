package com.kieronquinn.app.smartspacer.plugin.medicationreminder.di

import androidx.room.Room
import androidx.room.Room
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.MedicationReminderDatabase
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.MedicationRepository
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.ui.settings.AddMedicationViewModel
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.ui.settings.MedicationReminderSettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

import com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.SnoozeRepository

val medicationReminderModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            MedicationReminderDatabase::class.java,
            "medication_reminder_database"
        ).build()
    }
    single { get<MedicationReminderDatabase>().medicationDao() }
    single { MedicationRepository(get()) }
    single { SnoozeRepository(androidContext()) }

    viewModel { MedicationReminderSettingsViewModel(get()) }
    viewModel { AddMedicationViewModel(get()) }
}
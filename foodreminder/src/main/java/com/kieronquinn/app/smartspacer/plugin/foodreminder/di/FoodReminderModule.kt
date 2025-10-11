package com.kieronquinn.app.smartspacer.plugin.foodreminder.di

import androidx.room.Room
import androidx.room.Room
import androidx.room.Room
import com.kieronquinn.app.smartspacer.plugin.foodreminder.data.FoodItemRepository
import com.kieronquinn.app.smartspacer.plugin.foodreminder.data.FoodReminderDatabase
import com.kieronquinn.app.smartspacer.plugin.foodreminder.data.FoodReminderSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.foodreminder.ui.settings.FoodReminderSettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val foodReminderModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            FoodReminderDatabase::class.java,
            "food_reminder_database"
        ).build()
    }
    single { get<FoodReminderDatabase>().foodItemDao() }
    single { FoodItemRepository(get()) }
    single { FoodReminderSettingsRepository(androidContext()) }
    viewModel { FoodReminderSettingsViewModel(get(), get()) }
}
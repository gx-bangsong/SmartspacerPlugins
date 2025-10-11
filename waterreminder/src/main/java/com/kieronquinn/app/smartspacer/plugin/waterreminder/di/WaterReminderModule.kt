package com.kieronquinn.app.smartspacer.plugin.waterreminder.di

import com.kieronquinn.app.smartspacer.plugin.waterreminder.data.WaterReminderSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.waterreminder.ui.settings.WaterReminderSettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val waterReminderModule = module {
    single { WaterReminderSettingsRepository(androidContext()) }
    viewModel { WaterReminderSettingsViewModel(get()) }
}
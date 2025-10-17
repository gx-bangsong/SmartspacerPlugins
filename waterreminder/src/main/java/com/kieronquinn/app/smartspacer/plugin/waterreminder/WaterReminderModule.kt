package com.kieronquinn.app.smartspacer.plugin.waterreminder

import com.kieronquinn.app.smartspacer.plugin.waterreminder.data.WaterReminderSettings
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val waterReminderModule = module {
    single { WaterReminderSettings(androidContext()) }
}
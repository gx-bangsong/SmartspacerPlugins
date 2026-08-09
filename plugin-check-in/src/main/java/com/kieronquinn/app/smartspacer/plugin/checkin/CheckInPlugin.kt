package com.kieronquinn.app.smartspacer.plugin.checkin

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.checkin.data.CheckInDatabase
import com.kieronquinn.app.smartspacer.plugin.checkin.repositories.CheckInSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.checkin.repositories.CheckInSettingsRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.checkin.repositories.CheckInScheduler
import com.kieronquinn.app.smartspacer.plugin.checkin.repositories.CheckInSchedulerImpl
import com.kieronquinn.app.smartspacer.plugin.checkin.repositories.NavGraphRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.checkin.ui.fragments.CheckInSettingsViewModel
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class CheckInPlugin : SmartspacerPlugin() {

    override fun getModule(context: Context) = module {
        single { CheckInDatabase.getDatabase(get()).checkInDao() }
        single<CheckInSettingsRepository> { CheckInSettingsRepositoryImpl(get()) }
        single<CheckInScheduler> { CheckInSchedulerImpl(get(), get()) }
        single<NavGraphRepository> { NavGraphRepositoryImpl() }
        viewModel { CheckInSettingsViewModel(get(), get(), get()) }
    }
}

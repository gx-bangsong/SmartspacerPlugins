package com.kieronquinn.app.smartspacer.plugin.travel

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.travel.data.TravelInfoDatabase
import com.kieronquinn.app.smartspacer.plugin.travel.notifications.TravelNotificationController
import com.kieronquinn.app.smartspacer.plugin.travel.repositories.NavGraphRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.travel.repositories.TravelSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.travel.repositories.TravelSettingsRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.travel.repositories.TravelScheduler
import com.kieronquinn.app.smartspacer.plugin.travel.repositories.TravelSchedulerImpl
import com.kieronquinn.app.smartspacer.plugin.travel.repositories.TravelShareOperationRepository
import com.kieronquinn.app.smartspacer.plugin.travel.repositories.TravelSuppressionRepository
import com.kieronquinn.app.smartspacer.plugin.travel.ui.fragments.TravelSettingsViewModel
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class TravelPlugin : SmartspacerPlugin() {

    override fun getModule(context: Context) = module {
        single { TravelInfoDatabase.getDatabase(get()).travelInfoDao() }
        single<TravelSettingsRepository> { TravelSettingsRepositoryImpl(get()) }
        single<TravelScheduler> { TravelSchedulerImpl(get(), get()) }
        single { TravelShareOperationRepository(get()) }
        single { TravelSuppressionRepository(get()) }
        single { TravelNotificationController(get()) }
        single<NavGraphRepository> { NavGraphRepositoryImpl() }
        viewModel { TravelSettingsViewModel(get(), get(), get(), get(), get()) }
    }
}

package com.kieronquinn.app.smartspacer.plugin.water

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import com.kieronquinn.app.smartspacer.plugin.water.data.WaterDatabase
import com.kieronquinn.app.smartspacer.plugin.water.repositories.NavGraphRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.water.repositories.WaterDataRepository
import com.kieronquinn.app.smartspacer.plugin.water.repositories.WaterDataRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.water.scheduling.WaterScheduler
import com.kieronquinn.app.smartspacer.plugin.water.ui.screens.settings.WaterSettingsViewModel
import com.kieronquinn.app.smartspacer.plugin.water.ui.screens.settings.WaterSettingsViewModelImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class WaterPlugin: SmartspacerPlugin() {

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "water_reminder_channel"
    }

    override fun getModule(context: Context) = module {
        single { WaterDatabase.getDatabase(get()).drinkHistoryDao() }
        single<WaterDataRepository> { WaterDataRepositoryImpl(get(), get()) }
        single { WaterScheduler() }
        single<NavGraphRepository> { NavGraphRepositoryImpl() }
        viewModel<WaterSettingsViewModel> { WaterSettingsViewModelImpl(get(), get()) }
    }

}

package com.kieronquinn.app.smartspacer.plugin.qweather

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.qweather.providers.QWeatherRepository
import com.kieronquinn.app.smartspacer.plugin.qweather.providers.QWeatherRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.qweather.providers.SettingsRepository
import com.kieronquinn.app.smartspacer.plugin.qweather.providers.SettingsRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.qweather.repositories.NavGraphRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.qweather.retrofit.QWeatherClient
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import com.kieronquinn.app.smartspacer.plugin.qweather.ui.screens.settings.SettingsViewModel
import com.kieronquinn.app.smartspacer.plugin.qweather.ui.screens.settings.SettingsViewModelImpl

class QWeatherPlugin : SmartspacerPlugin() {

    override fun getModule(context: Context) = module {
        single<SettingsRepository> { SettingsRepositoryImpl(get()) }
        single { QWeatherClient(get()) }
        single<QWeatherRepository> { QWeatherRepositoryImpl() }
        single<NavGraphRepository> { NavGraphRepositoryImpl() }
        viewModel<SettingsViewModel> { SettingsViewModelImpl(get(), get()) }
    }

}

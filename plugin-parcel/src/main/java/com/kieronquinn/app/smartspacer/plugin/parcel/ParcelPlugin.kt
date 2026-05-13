package com.kieronquinn.app.smartspacer.plugin.parcel

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.parcel.data.ParcelDatabase
import com.kieronquinn.app.smartspacer.plugin.parcel.repositories.NavGraphRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import com.kieronquinn.app.smartspacer.plugin.parcel.ui.fragments.SettingsViewModel
import com.kieronquinn.app.smartspacer.plugin.parcel.ui.fragments.SettingsViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.parcel.work.ParcelWorker
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class ParcelPlugin : SmartspacerPlugin() {
    override fun onCreate() {
        super.onCreate()
        // 启动定期清理任务
        ParcelWorker.enqueuePeriodic(this)
    }

    override fun getModule(context: Context) = module {
        single { ParcelDatabase.getInstance(get()).parcelDao() }
        single { ParcelDatabase.getInstance(get()).ruleDao() }
        single<NavGraphRepository> { NavGraphRepositoryImpl() }
        viewModel<SettingsViewModel> { SettingsViewModelImpl(get(), get()) }
    }
}

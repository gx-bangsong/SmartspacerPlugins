package com.kieronquinn.app.smartspacer.plugin.parcel

import android.content.Context
import android.util.Log
import com.kieronquinn.app.smartspacer.plugin.parcel.data.ParcelDatabase
import com.kieronquinn.app.smartspacer.plugin.parcel.notifications.ParcelNotificationController
import com.kieronquinn.app.smartspacer.plugin.parcel.notifications.ParcelSuppressionRepository
import com.kieronquinn.app.smartspacer.plugin.parcel.repositories.NavGraphRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.parcel.repositories.SettingsRepository
import com.kieronquinn.app.smartspacer.plugin.parcel.repositories.SettingsRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import com.kieronquinn.app.smartspacer.plugin.parcel.ui.fragments.SettingsViewModel
import com.kieronquinn.app.smartspacer.plugin.parcel.ui.fragments.SettingsViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.parcel.permissions.ParcelPermissions
import com.kieronquinn.app.smartspacer.plugin.parcel.work.ParcelWorker
import com.kieronquinn.app.smartspacer.plugin.shared.permissions.PluginPermissionConfig
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class ParcelPlugin : SmartspacerPlugin() {
    override fun onCreate() {
        super.onCreate()
        // 启动定期清理任务
        try {
            ParcelWorker.enqueuePeriodic(this)
        } catch (e: Exception) {
            Log.e("ParcelPlugin", "Failed to enqueue periodic work", e)
        }
    }

    override fun getModule(context: Context) = module {
        single { ParcelDatabase.getInstance(get()).parcelDao() }
        single { ParcelDatabase.getInstance(get()).ruleDao() }
        single<SettingsRepository> { SettingsRepositoryImpl(get()) }
        single<PluginPermissionConfig> { ParcelPermissions.config }
        single { ParcelNotificationController(get()) }
        single { ParcelSuppressionRepository(get()) }
        single<NavGraphRepository> { NavGraphRepositoryImpl() }
        viewModel<SettingsViewModel> { SettingsViewModelImpl(get(), get(), get()) }
    }
}

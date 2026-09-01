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
import com.kieronquinn.app.smartspacer.plugin.travel.permissions.TravelPermissions
import com.kieronquinn.app.smartspacer.plugin.travel.ui.fragments.TravelSettingsViewModel
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.shared.permissions.ExactAlarmRescheduler
import com.kieronquinn.app.smartspacer.plugin.shared.permissions.PluginPermissionConfig
import com.kieronquinn.app.smartspacer.plugin.shared.permissions.SmsPermissionFallback
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class TravelPlugin : SmartspacerPlugin() {

    override fun getModule(context: Context) = module {
        single { TravelInfoDatabase.getDatabase(get()).travelInfoDao() }
        single<TravelSettingsRepository> { TravelSettingsRepositoryImpl(get()) }
        single { TravelShareOperationRepository(get()) }
        single { TravelSuppressionRepository(get()) }
        single { TravelNotificationController(get()) }
        single<TravelScheduler> { TravelSchedulerImpl(get(), get(), get(), get()) }
        single<PluginPermissionConfig> { TravelPermissions.config }
        single<ExactAlarmRescheduler> {
            ExactAlarmRescheduler { get<TravelScheduler>().rescheduleAll() }
        }
        single<SmsPermissionFallback> {
            val settings = get<TravelSettingsRepository>()
            SmsPermissionFallback {
                CoroutineScope(Dispatchers.IO).launch {
                    settings.setSmsParsingEnabled(false)
                }
            }
        }
        single<NavGraphRepository> { NavGraphRepositoryImpl() }
        viewModel { TravelSettingsViewModel(get(), get(), get(), get(), get()) }
    }
}

package com.kieronquinn.app.smartspacer.plugin.medication

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.medication.data.MedicationDatabase
import com.kieronquinn.app.smartspacer.plugin.medication.permissions.MedicationPermissions
import com.kieronquinn.app.smartspacer.plugin.medication.repositories.MedicationScheduler
import com.kieronquinn.app.smartspacer.plugin.medication.repositories.MedicationSchedulerImpl
import com.kieronquinn.app.smartspacer.plugin.medication.repositories.NavGraphRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.shared.permissions.ExactAlarmRescheduler
import com.kieronquinn.app.smartspacer.plugin.shared.permissions.PluginPermissionConfig
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import org.koin.dsl.module

class MedicationPlugin: SmartspacerPlugin() {

    override fun getModule(context: Context) = module {
        single { MedicationDatabase.getDatabase(get()).medicationDao() }
        single { MedicationDatabase.getDatabase(get()).doseHistoryDao() }
        single<NavGraphRepository> { NavGraphRepositoryImpl() }
        single<MedicationScheduler> { MedicationSchedulerImpl(get(), get()) }
        single<PluginPermissionConfig> { MedicationPermissions.config }
        single<ExactAlarmRescheduler> {
            ExactAlarmRescheduler { get<MedicationScheduler>().rescheduleAll() }
        }
    }

}

package com.kieronquinn.app.smartspacer.plugin.food

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.food.data.FoodDatabase
import com.kieronquinn.app.smartspacer.plugin.food.permissions.FoodPermissions
import com.kieronquinn.app.smartspacer.plugin.food.repositories.FoodScheduler
import com.kieronquinn.app.smartspacer.plugin.food.repositories.FoodSchedulerImpl
import com.kieronquinn.app.smartspacer.plugin.food.repositories.NavGraphRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.shared.permissions.ExactAlarmRescheduler
import com.kieronquinn.app.smartspacer.plugin.shared.permissions.PluginPermissionConfig
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import org.koin.dsl.module

class FoodPlugin: SmartspacerPlugin() {

    override fun getModule(context: Context) = module {
        single { FoodDatabase.getDatabase(get()).foodItemDao() }
        single<NavGraphRepository> { NavGraphRepositoryImpl() }
        single<FoodScheduler> { FoodSchedulerImpl(get(), get()) }
        single<PluginPermissionConfig> { FoodPermissions.config }
        single<ExactAlarmRescheduler> {
            ExactAlarmRescheduler { get<FoodScheduler>().rescheduleAll() }
        }
    }

}

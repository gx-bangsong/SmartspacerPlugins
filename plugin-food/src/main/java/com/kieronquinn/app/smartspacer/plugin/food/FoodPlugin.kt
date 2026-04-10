package com.kieronquinn.app.smartspacer.plugin.food

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.food.data.FoodDatabase
import com.kieronquinn.app.smartspacer.plugin.food.repositories.NavGraphRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import org.koin.dsl.module

class FoodPlugin: SmartspacerPlugin() {

    override fun getModule(context: Context) = module {
        single { FoodDatabase.getDatabase(get()).foodItemDao() }
        single<NavGraphRepository> { NavGraphRepositoryImpl() }
    }

}

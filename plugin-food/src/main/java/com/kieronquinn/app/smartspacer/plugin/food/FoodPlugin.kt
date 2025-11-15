package com.kieronquinn.app.smartspacer.plugin.food

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.food.data.FoodDatabase
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import org.koin.dsl.module

import org.koin.core.component.KoinComponent

class FoodPlugin: SmartspacerPlugin(), KoinComponent {

    override fun getModule(context: Context) = module {
        single { FoodDatabase.getDatabase(get()).foodItemDao() }
    }

}
package com.kieronquinn.app.smartspacer.plugin.foodreminder

import android.content.Context
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.foodreminder.worker.FoodReminderWorker   // ✅ 注意这里的 import
import java.util.concurrent.TimeUnit
import org.koin.core.module.Module
import org.koin.dsl.module

class FoodReminderPlugin : SmartspacerPlugin() {

override fun getModule(context: Context): Module {
    return module {
        single { FoodReminderSettings(context) }

        // 先创建 Room Database
        single {
            androidx.room.Room.databaseBuilder(
                context,
                com.kieronquinn.app.smartspacer.plugin.foodreminder.data.FoodItemDatabase::class.java,
                "food_item_database"
            ).build()
        }

        // 注入 Dao
        single { get<com.kieronquinn.app.smartspacer.plugin.foodreminder.data.FoodItemDatabase>().foodItemDao() }

        // 注入 Repository
        single { com.kieronquinn.app.smartspacer.plugin.foodreminder.data.FoodItemRepository(get()) }
    }
}


    override fun onCreate() {
        super.onCreate()
        val workRequest = PeriodicWorkRequestBuilder<FoodReminderWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(applicationContext).enqueue(workRequest)
    }
}

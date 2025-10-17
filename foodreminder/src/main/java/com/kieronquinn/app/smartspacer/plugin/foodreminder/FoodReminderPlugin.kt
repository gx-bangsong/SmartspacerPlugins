package com.kieronquinn.app.smartspacer.plugin.foodreminder

import android.content.Context
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.foodreminder.work.FoodReminderWorker   // ✅ 注意这里的 import
import java.util.concurrent.TimeUnit
import org.koin.core.module.Module

class FoodReminderPlugin : SmartspacerPlugin() {

    override fun getModule(context: Context): Module {
        return foodReminderModule
    }

    override fun onCreate() {
        super.onCreate()
        val workRequest = PeriodicWorkRequestBuilder<FoodReminderWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueue(workRequest)
    }
}

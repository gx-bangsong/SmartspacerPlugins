package com.kieronquinn.app.smartspacer.plugin.foodreminder

import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kieronquinn.app.smartspacer.sdk.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.foodreminder.worker.FoodReminderWorker
import org.koin.core.component.KoinComponent
import java.util.concurrent.TimeUnit

class FoodReminderPlugin: com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin() {
    override fun getModule(context: Context): Module {
        return foodReminderModule
    }

    override fun onCreate() {
        super.onCreate()
        val workRequest = PeriodicWorkRequestBuilder<FoodReminderWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueue(workRequest)
    }
}
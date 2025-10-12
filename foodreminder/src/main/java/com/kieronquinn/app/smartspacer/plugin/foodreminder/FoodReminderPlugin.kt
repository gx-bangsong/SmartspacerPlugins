package com.kieronquinn.app.smartspacer.plugin.foodreminder

import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kieronquinn.app.smartspacer.sdk.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.foodreminder.worker.FoodReminderWorker
import org.koin.core.component.KoinComponent
import java.util.concurrent.TimeUnit

class FoodReminderPlugin: SmartspacerPlugin(), KoinComponent {
    override fun getModules() = listOf(foodReminderModule)

    override fun onPluginEnabled() {
        super.onPluginEnabled()
        val workRequest = PeriodicWorkRequestBuilder<FoodReminderWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueue(workRequest)
    }
}
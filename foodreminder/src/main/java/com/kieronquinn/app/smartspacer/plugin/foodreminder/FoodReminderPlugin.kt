package com.kieronquinn.app.smartspacer.plugin.foodreminder

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kieronquinn.app.smartspacer.plugin.foodreminder.di.foodReminderModule
import com.kieronquinn.app.smartspacer.plugin.foodreminder.worker.FoodReminderWorker
import com.kieronquinn.app.smartspacer.sdk.SmartspacerPlugin
import org.koin.core.component.KoinComponent
import java.util.concurrent.TimeUnit

class FoodReminderPlugin : SmartspacerPlugin(), KoinComponent {

    override val modules = listOf(foodReminderModule)

    override fun onCreate() {
        super.onCreate()
        setupWorker()
    }

    private fun setupWorker() {
        val workRequest = PeriodicWorkRequestBuilder<FoodReminderWorker>(1, TimeUnit.DAYS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            FoodReminderWorker.WORKER_TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
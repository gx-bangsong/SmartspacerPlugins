package com.kieronquinn.app.smartspacer.plugin.waterreminder

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kieronquinn.app.smartspacer.plugin.waterreminder.di.waterReminderModule
import com.kieronquinn.app.smartspacer.plugin.waterreminder.worker.WaterReminderWorker
import com.kieronquinn.app.smartspacer.sdk.SmartspacerPlugin
import org.koin.core.component.KoinComponent
import java.util.concurrent.TimeUnit

class WaterReminderPlugin : SmartspacerPlugin(), KoinComponent {

    override val modules = listOf(waterReminderModule)

    override fun onCreate() {
        super.onCreate()
        setupWorker()
    }

    private fun setupWorker() {
        val workRequest = PeriodicWorkRequestBuilder<WaterReminderWorker>(1, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            WaterReminderWorker.WORKER_TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
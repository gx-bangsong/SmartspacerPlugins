package com.kieronquinn.app.smartspacer.plugin.waterreminder

import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kieronquinn.app.smartspacer.sdk.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.waterreminder.worker.WaterReminderWorker
import org.koin.core.component.KoinComponent
import java.util.concurrent.TimeUnit

class WaterReminderPlugin: com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin() {
    override fun getModule(context: Context): Module {
        return waterReminderModule
    }

    override fun onCreate() {
        super.onCreate()
        val workRequest = PeriodicWorkRequestBuilder<WaterReminderWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueue(workRequest)
        scheduleDailyReset()
    }

    private fun scheduleDailyReset() {
        val midnight = java.util.Calendar.getInstance().apply {
            add(java.util.Calendar.DAY_OF_YEAR, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
        }
        val delay = midnight.timeInMillis - System.currentTimeMillis()
        val workRequest = androidx.work.OneTimeWorkRequestBuilder<com.kieronquinn.app.smartspacer.plugin.waterreminder.worker.ResetWorker>()
            .setInitialDelay(delay, java.util.concurrent.TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(this).enqueue(workRequest)
    }
}
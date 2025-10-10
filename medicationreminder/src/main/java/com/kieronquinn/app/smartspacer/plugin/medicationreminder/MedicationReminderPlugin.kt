package com.kieronquinn.app.smartspacer.plugin.medicationreminder

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.di.medicationReminderModule
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.worker.MedicationReminderWorker
import com.kieronquinn.app.smartspacer.sdk.SmartspacerPlugin
import org.koin.core.component.KoinComponent
import java.util.concurrent.TimeUnit

class MedicationReminderPlugin : SmartspacerPlugin(), KoinComponent {

    override val modules = listOf(medicationReminderModule)

    override fun onCreate() {
        super.onCreate()
        setupWorker()
    }

    private fun setupWorker() {
        val workRequest = PeriodicWorkRequestBuilder<MedicationReminderWorker>(15, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            MedicationReminderWorker.WORKER_TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
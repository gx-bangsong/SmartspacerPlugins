package com.kieronquinn.app.smartspacer.plugin.medicationreminder

import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kieronquinn.app.smartspacer.sdk.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.worker.MedicationReminderWorker
import org.koin.core.component.KoinComponent
import java.util.concurrent.TimeUnit

class MedicationReminderPlugin: SmartspacerPlugin(), KoinComponent {
    override fun getModules() = listOf(medicationReminderModule)

    override fun onPluginEnabled() {
        super.onPluginEnabled()
        val workRequest = PeriodicWorkRequestBuilder<MedicationReminderWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueue(workRequest)
    }
}
package com.kieronquinn.app.smartspacer.plugin.medication.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.medication.repositories.MedicationScheduler
import com.kieronquinn.app.smartspacer.plugin.medication.work.MedicationWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * BootReceiver 负责在设备启动时重新调度 WorkManager 任务和闹钟。
 */
class BootReceiver : BroadcastReceiver(), KoinComponent {

    private val medicationScheduler by inject<MedicationScheduler>()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // 在启动时调度定期刷新任务
            MedicationWorker.enqueuePeriodic(context)

            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    medicationScheduler.rescheduleAll()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

}

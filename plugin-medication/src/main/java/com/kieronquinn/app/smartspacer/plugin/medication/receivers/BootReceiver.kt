package com.kieronquinn.app.smartspacer.plugin.medication.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.medication.work.MedicationWorker

/**
 * BootReceiver 负责在设备启动时重新调度 WorkManager 任务。
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // 在启动时调度定期刷新任务
            MedicationWorker.enqueuePeriodic(context)
        }
    }

}

package com.kieronquinn.app.smartspacer.plugin.water.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kieronquinn.app.smartspacer.plugin.water.scheduling.DailyScheduleWorker

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val workManager = WorkManager.getInstance(context)
            val request = OneTimeWorkRequestBuilder<DailyScheduleWorker>().build()
            workManager.enqueueUniqueWork("DailyScheduleWorker", ExistingWorkPolicy.REPLACE, request)
        }
    }

}
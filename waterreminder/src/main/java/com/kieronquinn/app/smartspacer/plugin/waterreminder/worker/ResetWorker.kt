package com.kieronquinn.app.smartspacer.plugin.waterreminder.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kieronquinn.app.smartspacer.plugin.waterreminder.data.WaterReminderSettings
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ResetWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val settings: WaterReminderSettings by inject()

    override suspend fun doWork(): Result {
        settings.currentIntake = 0
        return Result.success()
    }
}
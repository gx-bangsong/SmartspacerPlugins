package com.kieronquinn.app.smartspacer.plugin.water.work

import android.content.Context
import android.util.Log
import androidx.work.*
import com.kieronquinn.app.smartspacer.plugin.water.providers.WaterProvider
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import org.koin.core.component.KoinComponent
import java.util.concurrent.TimeUnit

/**
 * WaterWorker 负责刷新饮水提醒的 Smartspace Target。
 */
class WaterWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    companion object {
        private const val TAG = "WaterWorker"
        private const val WORK_NAME = "water_update_periodic"

        /**
         * 调度定期刷新任务。
         */
        fun enqueuePeriodic(context: Context) {
            val request = PeriodicWorkRequestBuilder<WaterWorker>(1, TimeUnit.HOURS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        /**
         * 调度即时刷新。
         */
        fun enqueueImmediate(context: Context) {
            val request = OneTimeWorkRequestBuilder<WaterWorker>()
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "water_update_immediate",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "正在刷新饮水提醒 Target")
        try {
            SmartspacerTargetProvider.notifyChange(context, WaterProvider::class.java)
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "刷新饮水提醒失败", e)
            return Result.retry()
        }
    }
}

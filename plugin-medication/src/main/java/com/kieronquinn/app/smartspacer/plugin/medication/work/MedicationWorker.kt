package com.kieronquinn.app.smartspacer.plugin.medication.work

import android.content.Context
import android.util.Log
import androidx.work.*
import com.kieronquinn.app.smartspacer.plugin.medication.providers.MedicationProvider
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import org.koin.core.component.KoinComponent
import java.util.concurrent.TimeUnit

/**
 * MedicationWorker 负责定期刷新服药提醒的 Smartspace Target。
 * 它在预定的服药时间前触发，以确保用户能在桌面上看到提醒。
 */
class MedicationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    companion object {
        private const val TAG = "MedicationWorker"
        private const val WORK_NAME = "medication_update_periodic"

        /**
         * 调度定期刷新任务，建议每小时检查一次。
         */
        fun enqueuePeriodic(context: Context) {
            val request = PeriodicWorkRequestBuilder<MedicationWorker>(1, TimeUnit.HOURS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        /**
         * 调度一次性立即刷新任务。
         */
        fun enqueueImmediate(context: Context) {
            val request = OneTimeWorkRequestBuilder<MedicationWorker>()
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "medication_update_immediate",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "正在刷新服药提醒 Target")
        try {
            // 通知 Smartspace 提供者更新内容
            SmartspacerTargetProvider.notifyChange(context, MedicationProvider::class.java)
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "刷新服药提醒失败", e)
            return Result.retry()
        }
    }
}

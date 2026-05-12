package com.kieronquinn.app.smartspacer.plugin.food.work

import android.content.Context
import android.util.Log
import androidx.work.*
import com.kieronquinn.app.smartspacer.plugin.food.providers.FoodProvider
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import org.koin.core.component.KoinComponent
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * FoodWorker 负责定期检查食品保质期并更新 Smartspace Target。
 */
class FoodWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    companion object {
        private const val TAG = "FoodWorker"
        private const val WORK_NAME = "food_update_periodic"

        /**
         * 调度每日定期刷新任务，建议在早上 8 点。
         */
        fun enqueuePeriodic(context: Context) {
            val calendar = Calendar.getInstance().apply {
                if (get(Calendar.HOUR_OF_DAY) >= 8) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
                set(Calendar.HOUR_OF_DAY, 8)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            val delay = calendar.timeInMillis - System.currentTimeMillis()

            val request = PeriodicWorkRequestBuilder<FoodWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
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
            val request = OneTimeWorkRequestBuilder<FoodWorker>()
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "food_update_immediate",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "正在检查食品保质期")
        try {
            SmartspacerTargetProvider.notifyChange(context, FoodProvider::class.java)
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "检查食品保质期失败", e)
            return Result.retry()
        }
    }
}

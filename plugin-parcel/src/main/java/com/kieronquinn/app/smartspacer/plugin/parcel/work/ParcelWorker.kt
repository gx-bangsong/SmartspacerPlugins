package com.kieronquinn.app.smartspacer.plugin.parcel.work

import android.content.Context
import androidx.work.*
import com.kieronquinn.app.smartspacer.plugin.parcel.data.ParcelDao
import com.kieronquinn.app.smartspacer.plugin.parcel.providers.ParcelTargetProvider
import com.kieronquinn.app.smartspacer.plugin.parcel.repositories.SettingsRepository
import com.kieronquinn.app.smartspacer.plugin.parcel.repositories.getBlocking
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

class ParcelWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params), KoinComponent {

    private val parcelDao by inject<ParcelDao>()
    private val settingsRepository by inject<SettingsRepository>()

    companion object {
        private const val WORK_NAME_PERIODIC = "parcel_periodic_work"
        private const val WORK_NAME_IMMEDIATE = "parcel_immediate_work"

        fun enqueuePeriodic(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val request = PeriodicWorkRequestBuilder<ParcelWorker>(1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME_PERIODIC,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun enqueueImmediate(context: Context) {
            val request = OneTimeWorkRequestBuilder<ParcelWorker>()
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME_IMMEDIATE,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }

    override suspend fun doWork(): Result {
        // 清理过期包裹（使用用户设置的时长）
        val now = System.currentTimeMillis()
        val durationHours = settingsRepository.cleanupDurationHours.getBlocking()
        parcelDao.markOldParcelsAsExpired(now - durationHours * 60 * 60 * 1000L)

        // 删除更久远的记录（超过7天）
        parcelDao.deleteOldParcels(now - 7 * 24 * 60 * 60 * 1000L)

        // 通知 Smartspace 刷新
        SmartspacerTargetProvider.notifyChange(applicationContext, ParcelTargetProvider::class.java)

        return Result.success()
    }
}

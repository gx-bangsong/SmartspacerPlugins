package com.kieronquinn.app.smartspacer.plugin.foodreminder.worker

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kieronquinn.app.smartspacer.plugin.foodreminder.data.FoodItemRepository
import com.kieronquinn.app.smartspacer.plugin.foodreminder.data.FoodReminderSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.foodreminder.providers.FoodReminderProvider
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

class FoodReminderWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    companion object {
        const val WORKER_TAG = "food_reminder_worker"
    }

    private val foodItemRepository: FoodItemRepository by inject()
    private val settingsRepository: FoodReminderSettingsRepository by inject()

    override suspend fun doWork(): Result {
        Log.d(WORKER_TAG, "Worker running...")
        val leadTimeDays = settingsRepository.reminderLeadTimeDays.get()
        val expiringItems = foodItemRepository.getFoodItems().first().filter {
            val daysUntilExpiry = TimeUnit.MILLISECONDS.toDays(it.expiryDate - System.currentTimeMillis())
            daysUntilExpiry in 0..leadTimeDays
        }

        if (expiringItems.isNotEmpty()) {
            Log.d(WORKER_TAG, "Found ${expiringItems.size} expiring items. Notifying provider.")
            // Notify the provider to refresh its targets
            val intent = Intent(appContext, FoodReminderProvider::class.java).apply {
                action = FoodReminderProvider.ACTION_REFRESH
            }
            appContext.sendBroadcast(intent)
        } else {
            Log.d(WORKER_TAG, "No expiring items found within the $leadTimeDays-day lead time.")
        }

        return Result.success()
    }
}
package com.kieronquinn.app.smartspacer.plugin.foodreminder.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kieronquinn.app.smartspacer.plugin.foodreminder.R
import com.kieronquinn.app.smartspacer.plugin.foodreminder.data.FoodItemRepository
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class FoodReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val repository: FoodItemRepository by inject()
    private val settings: com.kieronquinn.app.smartspacer.plugin.foodreminder.FoodReminderSettings by inject()

    override suspend fun doWork(): Result {
        val foodItems = repository.allFoodItems.first()
        val reminderTimeframe = settings.reminderTimeframe
        val expiringItems = foodItems.filter {
            val diff = it.expiryDate - System.currentTimeMillis()
            val days = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diff)
            days in 0..reminderTimeframe
        }

        expiringItems.forEach {
            sendNotification(it)
        }

        return Result.success()
    }

    private fun sendNotification(foodItem: com.kieronquinn.app.smartspacer.plugin.foodreminder.data.FoodItem) {
        val channelId = "food_reminder_channel"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Food Expiry Reminders",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = androidx.core.app.NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Food Expiring Soon")
            .setContentText("${foodItem.name} is expiring soon!")
            .setSmallIcon(R.drawable.ic_notification)
            .build()

        notificationManager.notify(foodItem.id, notification)
    }
}